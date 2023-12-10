package org.example.nicol.infrastructure.enhance

interface EventConsumer<T> {
    fun consumeEvent(event: T)
}