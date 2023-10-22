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

package org.example.nicol.application.mq.distrubute

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.example.nicol.application.config.NicolProperties
import org.example.nicol.application.mq.worker.DefaultStreamListener
import org.example.nicol.domain.system.model.result.UserResult
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine

@Aspect
@Component
class UserRegisteredDistributor(
    val redisTemplate: RedisTemplate<String, String>,
    val nicolProperties: NicolProperties
) {
    private val logger = LoggerFactory.getLogger(UserRegisteredDistributor::class.java)

    @AfterReturning(
        pointcut = "execution(* org.example.nicol.application.UserService.registerUser(..))",
        returning = "userResult"
    )
    fun doAfterReturning(userResult: UserResult) {
        val record = StreamRecords.newRecord().ofObject(userResult).withStreamKey(nicolProperties.mail.topic)
        val recordId = redisTemplate.opsForStream<String, String>().add(record)
        logger.debug("Push user message {} to mq success.",recordId)
    }
}