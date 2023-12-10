package org.example.nicol.infrastructure.enhance.ratelimit

import java.time.Duration

interface RateLimiter {

    fun of(name:String, rateLimiterConfig: RateLimiterConfig)  = AtomicRateLimiter(name, rateLimiterConfig)

    fun ofDefaults(name:String) = AtomicRateLimiter(name,RateLimiterConfig.ofDefaults())

    fun changeTimeOutDuration(timeoutDuration: Duration)

    fun changeLimitForPeriod(limitForPeriod: Int)

    fun acquirePermission(permits: Int):Boolean

    fun reservePermission(permits: Int):Boolean

    fun drainPermissions(permits: Int)

    fun name():String

    fun rateLimiterConfig():RateLimiterConfig

    fun eventPublisher()
}