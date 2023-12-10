package org.example.nicol.infrastructure.enhance.ratelimit.event

class RateLimiterOnSuccessEvent(rateLimiterName: String, numberOfPermits: Int) :
    AbstractRateLimiterEvent(rateLimiterName, numberOfPermits) {
    override fun eventType() = RateLimiterEvent.Type.SUCCESSFUL_ACQUIRE
}