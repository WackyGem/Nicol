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

package org.example.nicol.interfaces.restful

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.Valid
import org.example.nicol.application.InferenceService
import org.example.nicol.interfaces.config.event.SseEmitterFactory
import org.example.nicol.interfaces.config.version.ApiVersion
import org.example.nicol.interfaces.payload.mapper.ChatDtoMapper
import org.example.nicol.interfaces.payload.request.ChatCompletionsRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@ApiVersion
@RestController
@RequestMapping("/chat")
class ChatController(
    private val inferenceService: InferenceService,
    private val chatDtoMapper: ChatDtoMapper,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val sseEmitterFactory: SseEmitterFactory
) {

    @PostMapping("/completions")
    fun completions(@Valid @RequestBody chatCompletionsRequest: ChatCompletionsRequest): Any =
        if (chatCompletionsRequest.stream) {
            val streamChat = inferenceService.streamChat(chatDtoMapper.toChatCommand(chatCompletionsRequest))
            val emitter = sseEmitterFactory.createInstance()
            threadPoolTaskExecutor.execute {
                streamChat.forEach {
                    emitter.send(chatDtoMapper.toStreamChatResponse(it))
                }
                emitter.send("[DONE]")
                emitter.complete()
            }
            emitter.delegate
        } else {
            val chatResult = inferenceService.chat(chatDtoMapper.toChatCommand(chatCompletionsRequest))
            ResponseEntity.ok(chatDtoMapper.toChatResponse(chatResult))
        }

}