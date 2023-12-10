package org.example.nicol.infrastructure.enhance.ratelimit

import org.example.nicol.infrastructure.enhance.Either
import java.time.Duration
import java.util.function.Predicate

class RateLimiterConfig private constructor(
    val timeoutDuration: Duration = Duration.ofSeconds(5),
    val limitRefreshPeriod: Duration = Duration.ofNanos(500),
    val limitForPeriod: Int = 50,
    val drainPermissionsOnResult: Predicate<Either<Throwable, *>> = Predicate { false },
    val writableStackTraceEnabled: Boolean = true
) {
    companion object {
        private fun custom() = Builder()

        fun from(prototype: RateLimiterConfig) = Builder(prototype)

        fun ofDefaults() = Builder().build()
    }


    fun RateLimiterConfig(
        config: Builder.() -> Unit
    ) = custom().apply(config).build()

    fun RateLimiterConfig(
        baseConfig: RateLimiterConfig,
        config: Builder.() -> Unit
    ) = from(baseConfig).apply(config).build()

    data class Builder(
        private var timeoutDuration: Duration = Duration.ofSeconds(5),
        private var limitRefreshPeriod: Duration = Duration.ofNanos(500),
        private var limitForPeriod: Int = 50,
        private var drainPermissionsOnResult: Predicate<Either<Throwable, *>> = Predicate { false },
        private var writableStackTraceEnabled: Boolean = true
    ) {
        constructor(prototype: RateLimiterConfig) : this() {
            apply {
                timeoutDuration = prototype.timeoutDuration
                limitRefreshPeriod = prototype.limitRefreshPeriod
                limitForPeriod = prototype.limitForPeriod
                drainPermissionsOnResult = prototype.drainPermissionsOnResult
                writableStackTraceEnabled = prototype.writableStackTraceEnabled
            }
        }

        fun timeoutDuration(timeoutDuration: Duration) =
            apply { this.timeoutDuration = checkTimeoutDuration(timeoutDuration) }

        fun limitRefreshPeriod(limitRefreshPeriod: Duration) =
            apply { this.limitRefreshPeriod = checkLimitRefreshPeriod(limitRefreshPeriod) }

        fun limitForPeriod(limitForPeriod: Int) =
            apply { this.limitForPeriod = checkLimitForPeriod(limitForPeriod) }

        fun drainPermissionsOnResult(drainPermissionsOnResult: Predicate<Either<Throwable, *>>) =
            apply { this.drainPermissionsOnResult = drainPermissionsOnResult }

        fun writableStackTraceEnabled(writableStackTraceEnabled: Boolean) =
            apply { this.writableStackTraceEnabled = writableStackTraceEnabled }

        fun build() = RateLimiterConfig(
            timeoutDuration,
            limitRefreshPeriod,
            limitForPeriod,
            drainPermissionsOnResult,
            writableStackTraceEnabled
        )

        private fun checkTimeoutDuration(timeoutDuration: Duration): Duration {
            require(timeoutDuration >= Duration.ZERO) { "TimeoutDuration must not be negative" }
            return validateDurationWithinRange(timeoutDuration, "TimeoutDuration too large")
        }

        private fun validateDurationWithinRange(duration: Duration, message: String): Duration =
            try {
                Duration.ofNanos(duration.toNanos()) // make sure there is no long overflow
            } catch (e: Exception) {
                throw RuntimeException(message, e)
            }

        private fun checkLimitRefreshPeriod(limitRefreshPeriod: Duration): Duration {
            val validatedDuration = validateDurationWithinRange(
                limitRefreshPeriod,
                "LimitRefreshPeriod too large"
            )
            if (validatedDuration < Duration.ofNanos(1L)) {
                throw IllegalArgumentException("LimitRefreshPeriod is too short")
            }
            return validatedDuration
        }

        private fun checkLimitForPeriod(limitForPeriod: Int): Int {
            require(limitForPeriod > 0) { "LimitForPeriod should be greater than 0" }
            return limitForPeriod
        }
    }
}

