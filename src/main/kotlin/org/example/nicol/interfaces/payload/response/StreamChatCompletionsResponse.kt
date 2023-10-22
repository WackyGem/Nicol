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

package org.example.nicol.interfaces.payload.response

import com.fasterxml.jackson.annotation.JsonGetter
import org.example.nicol.domain.inference.enums.FinishReason
import org.example.nicol.domain.inference.enums.MessageRole
import java.time.OffsetDateTime

data class StreamChatCompletionsResponse(
    val id: String,
    val `object`: String,
    val created: OffsetDateTime,
    val model: String,
    val choices: List<Choice>,
) {
    data class Choice(
        val index: Int,
        val delta: Delta?,
        val finishReason: FinishReason?
    ) {
        @JsonGetter("finishReason")
        fun getFinishReason(): String? {
            return finishReason?.name?.lowercase()
        }
    }

    data class Delta(
        val role: MessageRole?,
        val content: String?
    ) {
        @JsonGetter("role")
        fun getRole(): String? {
            return role?.name?.lowercase()
        }
    }

    @JsonGetter("created")
    fun getCreatedSecond(): Long {
        return created.toEpochSecond()
    }
}