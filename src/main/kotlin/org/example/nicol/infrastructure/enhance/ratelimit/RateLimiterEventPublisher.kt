package org.example.nicol.infrastructure.enhance.ratelimit

import org.example.nicol.infrastructure.enhance.EventConsumer
import org.example.nicol.infrastructure.enhance.EventPublisher
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterEvent
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterOnFailureEvent
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterOnSuccessEvent

interface RateLimiterEventPublisher : EventPublisher<RateLimiterEvent> {
    fun onSuccess(onSuccessEventConsumer: EventConsumer<RateLimiterOnSuccessEvent>): RateLimiterEventPublisher

    fun onFailure(onFailureEventConsumer: EventConsumer<RateLimiterOnFailureEvent>): RateLimiterEventPublisher
}