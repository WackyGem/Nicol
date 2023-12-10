package org.example.nicol.infrastructure.enhance.ratelimit.event

class RateLimiterOnFailureEvent(rateLimiterName: String, numberOfPermits: Int) :
    AbstractRateLimiterEvent(rateLimiterName, numberOfPermits) {
    override fun eventType() = RateLimiterEvent.Type.FAILED_ACQUIRE
}