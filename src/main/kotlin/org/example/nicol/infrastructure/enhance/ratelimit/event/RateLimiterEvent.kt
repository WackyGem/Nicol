package org.example.nicol.infrastructure.enhance.ratelimit.event

import java.time.ZonedDateTime

interface RateLimiterEvent {
    fun eventType(): Type
    fun rateLimiterName(): String
    fun numberOfPermits(): Int
    fun creationTime(): ZonedDateTime

    enum class Type {
        FAILED_ACQUIRE,
        SUCCESSFUL_ACQUIRE,
        DRAINED
    }
}