/*
 * MIT License
 *
 * Copyright (c) 2023 Wacky Gem
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.example.nicol.application.mq.worker

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.example.nicol.application.config.NicolProperties
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.Subscription
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class DefaultStreamConsumer(
    val redisConnectionFactory: RedisConnectionFactory,
    val nicolProperties: NicolProperties,
    val redisTemplate: RedisTemplate<String, String>,
) {

    private final lateinit var container: StreamMessageListenerContainer<String, MapRecord<String, String, String>>
    private final var subscriptions: MutableList<Subscription> = mutableListOf()
    private val logger = LoggerFactory.getLogger(DefaultStreamConsumer::class.java)

    @PostConstruct
    fun initialize() {
        logger.info("StreamMessageListenerContainer - Initializing...")
        val containerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1L))
            .build()
        container = StreamMessageListenerContainer.create(redisConnectionFactory, containerOptions)
        nicolProperties.mq.streams.forEach { streamProperties ->
            streamProperties.groups.forEach { groupProperties ->
                initConsumerGroup(streamProperties.topic, groupProperties.name)
                groupProperties.consumers.forEach { consumerName ->
                    val subscription = container.receive(
                        Consumer.from(groupProperties.name, consumerName),
                        StreamOffset.create(streamProperties.topic, ReadOffset.lastConsumed()),
                        DefaultStreamListener(
                            groupProperties.name,
                            consumerName,
                            redisTemplate,
                            groupProperties.callback
                        )
                    )
                    subscriptions.add(subscription)
                }
            }
        }
        container.start()
        logger.info("StreamMessageListenerContainer - Start completed.")
    }

    private fun initConsumerGroup(topic: String, groupName: String) {
        val hasTopic = redisTemplate.hasKey(topic)
        if (!hasTopic) {
            redisTemplate.opsForStream<String, String>().createGroup(topic, groupName);
        } else {
            val info = redisTemplate.opsForStream<String, String>().groups(topic)
            if (info.any { it.groupName() != groupName }) {
                redisTemplate.opsForStream<String, String>().createGroup(topic, groupName);
            }
        }
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("StreamMessageListenerContainer - Shutdown initiated...")
        container.stop()
        while (subscriptions.any { it.isActive }) {
            Thread.sleep(1 * 1000L)
        }
        logger.info("StreamMessageListenerContainer - Shutdown completed.")
    }
}