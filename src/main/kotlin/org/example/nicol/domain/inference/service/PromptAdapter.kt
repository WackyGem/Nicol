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

package org.example.nicol.domain.inference.service

import org.example.nicol.domain.inference.enums.MessageRole
import org.example.nicol.domain.inference.enums.TransformerType
import org.example.nicol.domain.inference.model.command.ChatCommand
import org.example.nicol.infrastructure.enhance.strategy.StrategyKey
import org.springframework.stereotype.Component

interface PromptAdapter {
    @get:StrategyKey
    val type: TransformerType

    fun buildPromptTemplate(messages: List<ChatCommand.Message>): String
}

@Component
class Llama2PromptAdapter : PromptAdapter {
    override val type: TransformerType
        get() = TransformerType.LLama2


    override fun buildPromptTemplate(messages: List<ChatCommand.Message>): String {
        if (messages.isEmpty()) {
            return ""
        }

        val isNeedSystemPrompt = messages[0].role == MessageRole.SYSTEM
        val instStartTag = "[INST]"
        val instEndTag = "[/INST]"
        val sysStartTag = "<<SYS>>"
        val sysEndTag = "<</SYS>>"
        return messages.joinToString("") { message ->
            when (message.role) {
                MessageRole.SYSTEM -> "$instStartTag $sysStartTag\n${message.content}\n$sysEndTag\n\n"
                MessageRole.USER -> "${message.content}$instEndTag"
                MessageRole.ASSISTANT -> "${message.content} </s><s>[INST]"
            }
        }.let { if (isNeedSystemPrompt) it else instStartTag.plus(it) }
    }
}