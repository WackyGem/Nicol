package org.example.nicol.interfaces.config.ratelimit

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RateLimiter(
    val bindKey: String = "all",
    val permitsPerSecond: Double,
    val maxRequestLimit: Double,
    val fallbackMethod: String
)

