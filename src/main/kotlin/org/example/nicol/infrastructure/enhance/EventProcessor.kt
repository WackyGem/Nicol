package org.example.nicol.infrastructure.enhance

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList

open class EventProcessor<T> : EventPublisher<T> {

    private val onEventConsumers = CopyOnWriteArrayList<EventConsumer<T>>()
    private val eventConsumerMap = ConcurrentHashMap<String, List<EventConsumer<T>>>()
    private var consumerRegistered = false

    fun hasConsumers(): Boolean {
        return consumerRegistered
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun registerConsumer(className: String, eventConsumer: EventConsumer<out T>) {
        eventConsumerMap.compute(className) { _, consumers ->
            val updatedConsumers = if (consumers == null) {
                CopyOnWriteArrayList<EventConsumer<T>>().apply { add(eventConsumer as EventConsumer<T>) }
            } else {
                consumers.toMutableList().apply { add(eventConsumer as EventConsumer<T>) }
            }
            consumerRegistered = true
            updatedConsumers
        }
    }

    fun <E : T> processEvent(event: E): Boolean {
        var consumed = false

        if (onEventConsumers.isNotEmpty()) {
            onEventConsumers.forEach { it.consumeEvent(event) }
            consumed = true
        }

        val consumers = eventConsumerMap[event!!::class.java.name]
        if (consumers?.isNotEmpty() == true) {
            consumers.forEach { it.consumeEvent(event) }
            consumed = true
        }

        return consumed
    }

    @Synchronized
    override fun onEvent(onEventConsumer: EventConsumer<T>) {
        onEventConsumers.add(onEventConsumer)
        consumerRegistered = true
    }
}