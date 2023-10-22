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

import org.example.nicol.application.mq.callback.CallBackFunctionRegistry
import org.example.nicol.application.mq.callback.CallbackFunctionType
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamListener

class DefaultStreamListener(
    private val groupName: String,
    private val consumerName: String,
    private val redisTemplate:RedisTemplate<String, String>,
    private val callbackFunctionType: CallbackFunctionType
) : StreamListener<String, MapRecord<String, String, String>> {

    private val logger = LoggerFactory.getLogger(DefaultStreamListener::class.java)

    override fun onMessage(message: MapRecord<String, String, String>) {
        redisTemplate.opsForStream<String, String>().acknowledge(groupName, message)
        logger.debug("group: {} ,consumer: {} , topic: {} , messageId: {}  , value: {}",
            groupName, consumerName,message.stream, message.id ,message.value)
        CallBackFunctionRegistry.getStrategy(callbackFunctionType).invoke(message)
    }
}