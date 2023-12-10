package org.example.nicol.infrastructure.enhance.ratelimit

import org.example.nicol.infrastructure.enhance.EventConsumer
import org.example.nicol.infrastructure.enhance.EventProcessor
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterEvent
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterOnFailureEvent
import org.example.nicol.infrastructure.enhance.ratelimit.event.RateLimiterOnSuccessEvent


class RateLimiterEventProcessor : EventProcessor<RateLimiterEvent>(),
    EventConsumer<RateLimiterEvent>,
    RateLimiterEventPublisher {
    override fun consumeEvent(event: RateLimiterEvent) {
        super.processEvent(event)
    }

    override fun onSuccess(onSuccessEventConsumer: EventConsumer<RateLimiterOnSuccessEvent>):
            RateLimiterEventPublisher {
        registerConsumer(RateLimiterOnSuccessEvent::class.java.name, onSuccessEventConsumer)
        return this
    }

    override fun onFailure(onFailureEventConsumer: EventConsumer<RateLimiterOnFailureEvent>):
            RateLimiterEventPublisher {
        registerConsumer(RateLimiterOnFailureEvent::class.java.name, onFailureEventConsumer)
        return this
    }

}