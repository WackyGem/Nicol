package org.example.nicol.infrastructure.enhance.ratelimit.event

import java.time.ZonedDateTime

abstract class AbstractRateLimiterEvent protected constructor(
    private val rateLimiterName: String,
    private val numberOfPermits: Int,
) : RateLimiterEvent {

    private val creationTime: ZonedDateTime = ZonedDateTime.now()

    override fun rateLimiterName(): String = rateLimiterName

    override fun numberOfPermits(): Int = numberOfPermits

    override fun creationTime(): ZonedDateTime = creationTime
}