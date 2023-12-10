package org.example.nicol.infrastructure.enhance

interface EventPublisher<T> {
    fun onEvent(onEventConsumer: EventConsumer<T>)
}