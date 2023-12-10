package org.example.nicol.infrastructure.enhance.ratelimit

import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterOnFailureEvent
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterOnSuccessEvent
import java.lang.Integer.min
import java.lang.System.nanoTime
import java.lang.Thread.currentThread
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport.parkNanos


class AtomicRateLimiter(
    private val name: String,
    private val rateLimiterConfig: RateLimiterConfig
) : RateLimiter {

    private var nanoTimeStart: Long = nanoTime()

    private var waitingThreads: AtomicInteger = AtomicInteger(0);

    private val state: AtomicReference<State> = AtomicReference(
        State(
            rateLimiterConfig,
            0,
            rateLimiterConfig.limitForPeriod,
            0
        )
    )

    private val eventProcessor: RateLimiterEventProcessor = RateLimiterEventProcessor()

    data class State(
        val config: RateLimiterConfig,
        val activeCycle: Long,
        val activePermission: Int,
        val nanosToWait: Long
    )

    private fun divCeil(x: Int, y: Int): Int = (x + y - 1) / y

    private fun nanosToWaitForPermission(
        permits: Int,
        cyclePeriodInNanos: Long,
        permissionsPerCycle: Int,
        availablePermissions: Int,
        currentNanos: Long,
        currentCycle: Long
    ): Long = if (availablePermissions >= permits) {
        0L
    } else {
        val nextCycleTimeInNanos = (currentCycle + 1) * cyclePeriodInNanos
        val nanosToNextCycle = nextCycleTimeInNanos - currentNanos
        val permissionsAtTheStartOfNextCycle = availablePermissions + permissionsPerCycle
        val fullCyclesToWait = divCeil(-(permissionsAtTheStartOfNextCycle - permits), permissionsPerCycle);
        (fullCyclesToWait * cyclePeriodInNanos) + nanosToNextCycle
    }

    private fun compareAndSet(current: State, next: State): Boolean {
        if (state.compareAndSet(current, next)) {
            return true
        }
        parkNanos(1)
        return false
    }

    private fun reservePermissions(
        config: RateLimiterConfig,
        permits: Int,
        timeoutInNanos: Long,
        cycle: Long,
        permissions: Int,
        nanosToWait: Long
    ): AtomicRateLimiter.State {
        val canAcquireInTime: Boolean = timeoutInNanos >= nanosToWait
        var permissionsWithReservation: Int = permissions
        if (canAcquireInTime) {
            permissionsWithReservation -= permits
        }
        return State(config, cycle, permissionsWithReservation, nanosToWait)
    }

    private fun calculateNextState(
        permits: Int, timeoutInNanos: Long,
        activeState: State
    ): State {
        val cyclePeriodInNanos: Long = activeState.config.limitRefreshPeriod.toNanos()
        val permissionsPerCycle: Int = activeState.config.limitForPeriod
        val currentNanos: Long = nanoTime() - nanoTimeStart;
        val currentCycle = currentNanos / cyclePeriodInNanos
        var nextCycle = activeState.activeCycle
        var nextPermissions: Int = activeState.activePermission
        if (nextCycle != currentCycle) {
            val elapsedCycles: Int = (currentCycle - nextCycle).toInt()
            val accumulatedPermissions: Int = elapsedCycles * permissionsPerCycle
            nextCycle = currentCycle
            nextPermissions = min(
                nextPermissions + accumulatedPermissions,
                permissionsPerCycle
            )
        }
        val nextNanosToWait: Long = nanosToWaitForPermission(
            permits, cyclePeriodInNanos, permissionsPerCycle, nextPermissions, currentNanos,
            currentCycle
        )
        val nextState: State = reservePermissions(
            activeState.config, permits, timeoutInNanos, nextCycle,
            nextPermissions, nextNanosToWait
        )
        return nextState
    }

    private fun updateStateWithBackOff(permits: Int, timeoutInNanos: Long): State {
        var prev: State
        var next: State
        do {
            prev = state.get();
            next = calculateNextState(permits, timeoutInNanos, prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    private fun waitForPermissionIfNecessary(timeoutInNanos: Long, nanosToWait: Long): Boolean {
        val canAcquireImmediately = nanosToWait <= 0
        val canAcquireInTime = timeoutInNanos >= nanosToWait

        if (canAcquireImmediately) {
            return true
        }
        if (canAcquireInTime) {
            return waitForPermission(nanosToWait)
        }
        waitForPermission(timeoutInNanos)
        return false
    }

    private fun waitForPermission(nanosToWait: Long): Boolean {
        waitingThreads.incrementAndGet()
        val deadline: Long = nanoTime() - nanoTimeStart + nanosToWait
        var wasInterrupted = false
        while ((nanoTime() - nanoTimeStart) < deadline && !wasInterrupted) {
            val sleepBlockDuration: Long = deadline - (nanoTime() - nanoTimeStart)
            parkNanos(sleepBlockDuration)
            wasInterrupted = Thread.interrupted()
        }
        waitingThreads.decrementAndGet()
        if (wasInterrupted) {
            currentThread().interrupt()
        }
        return !wasInterrupted
    }

    private fun publishRateLimiterAcquisitionEvent(permissionAcquired: Boolean, permits: Int) {
        if (!eventProcessor.hasConsumers()) {
            return
        }
        if (permissionAcquired) {
            eventProcessor.consumeEvent(RateLimiterOnSuccessEvent(name, permits))
            return
        }
        eventProcessor.consumeEvent(RateLimiterOnFailureEvent(name, permits))
    }

    override fun changeTimeOutDuration(timeoutDuration: Duration) {
        val newConfig = RateLimiterConfig.from(state.get().config)
            .timeoutDuration(timeoutDuration)
            .build();
        state.updateAndGet { State(newConfig, it.activeCycle, it.activePermission, it.nanosToWait) }
    }

    override fun changeLimitForPeriod(limitForPeriod: Int) {
        val newConfig = RateLimiterConfig.from(state.get().config)
            .limitForPeriod(limitForPeriod)
            .build();
        state.updateAndGet { State(newConfig, it.activeCycle, it.activePermission, it.nanosToWait) }
    }

    override fun acquirePermission(permits: Int): Boolean {
        val timeoutInNanos = state.get().config.timeoutDuration.toNanos()
        val modifiedState: State = updateStateWithBackOff(permits, timeoutInNanos)
        val result: Boolean = waitForPermissionIfNecessary(timeoutInNanos, modifiedState.nanosToWait)
        publishRateLimiterAcquisitionEvent(result, permits)
        return result;
    }

    override fun reservePermission(permits: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun drainPermissions(permits: Int) {
        TODO("Not yet implemented")
    }

    override fun name() = this.name

    override fun rateLimiterConfig() = this.rateLimiterConfig

    override fun eventPublisher() {
        TODO("Not yet implemented")
    }
}