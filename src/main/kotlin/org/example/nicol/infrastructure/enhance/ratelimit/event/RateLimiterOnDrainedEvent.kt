package org.example.nicol.infrastructure.enhance.ratelimit.event

class RateLimiterOnDrainedEvent(rateLimiterName: String, numberOfPermits: Int) :
    AbstractRateLimiterEvent(rateLimiterName, numberOfPermits) {
    override fun eventType() = RateLimiterEvent.Type.DRAINED
}