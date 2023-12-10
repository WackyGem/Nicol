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

package org.example.nicol.domain.inference

import jakarta.annotation.PostConstruct
import org.example.nicol.application.InferenceService
import org.example.nicol.application.config.NicolProperties
import org.example.nicol.domain.inference.enums.FinishReason
import org.example.nicol.domain.inference.enums.MessageRole
import org.example.nicol.domain.inference.model.mapper.ChatCommandMapper
import org.example.nicol.domain.inference.enums.TransformerType
import org.example.nicol.domain.inference.model.command.ChatCommand
import org.example.nicol.domain.inference.model.result.ChatResult
import org.example.nicol.domain.inference.model.result.ModelsResult
import org.example.nicol.domain.inference.model.result.StreamChatResult
import org.example.nicol.domain.inference.service.Downloader
import org.example.nicol.domain.inference.service.PromptAdapter
import org.example.nicol.domain.inference.service.Tokenizer
import org.example.nicol.domain.inference.service.Transformer
import org.example.nicol.infrastructure.enhance.strategy.StrategyMap
import org.example.nicol.infrastructure.entity.ModelWeight
import org.example.nicol.infrastructure.entity.Vocabulary
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class InferenceServiceImpl(
    private val nicolProperties: NicolProperties,
    private val chatCommandMapper: ChatCommandMapper,
    private val tokenizer: Tokenizer,
    private val downloader: Downloader
) : InferenceService {

    private val logger = LoggerFactory.getLogger(InferenceServiceImpl::class.java)

    private final lateinit var modelWeight: ModelWeight

    private final lateinit var vocabulary: Vocabulary

    private final val checkPointDownloadUrl: String =
        "https://huggingface.co/karpathy/tinyllamas/resolve/main/stories15M.bin"

    private final val tokenizerDownloadUrl: String =
        "https://raw.githubusercontent.com/karpathy/llama2.c/master/tokenizer.bin"

    @StrategyMap
    private lateinit var transformerStrategies: Map<TransformerType, Transformer>

    @StrategyMap
    private lateinit var promptAdapterStrategies: Map<TransformerType, PromptAdapter>

    @PostConstruct
    fun initialize() {
        val checkPointPath = nicolProperties.inference.model.checkPoint
        downloader.checkAndDownloadFile(
            checkPointPath.substringBeforeLast('/'),
            checkPointPath.substringAfterLast('/'),
            checkPointDownloadUrl
        )
        val tokenizerPath = nicolProperties.inference.tokenizer.path
        downloader.checkAndDownloadFile(
            tokenizerPath.substringBeforeLast('/'),
            tokenizerPath.substringAfterLast('/'),
            tokenizerDownloadUrl
        )
        modelWeight = ModelWeight(checkPointPath)
        vocabulary = Vocabulary(tokenizerPath, modelWeight)
    }

    override fun models(): ModelsResult = ModelsResult(
        models = TransformerType.entries.map {
            ModelsResult.ModelResult(
                id = 0L,
                name = it.name.lowercase(),
                createdAt = OffsetDateTime.now(),
                username = "Nicol"
            )
        }
    )

    override fun chat(chatCommand: ChatCommand): ChatResult {
        val transformerType = TransformerType.getByLowercase(chatCommand.model)
        val promptInfo = checkAndBuildPrompt(transformerType, chatCommand.messages)
        val step = minOf(chatCommand.maxTokens, modelWeight.config.seqLen)
        val tokens = generateTokens(transformerType, chatCommand, promptInfo, step)
        val output = tokens.drop(promptInfo.numPromptTokens)
            .map { tokenizer.decode(vocabulary, it) }
            .joinToString("")
        logger.debug("prompt: {} \n output: {} \n", promptInfo.prompt, output)
        val totalTokens = tokens.count()
        return ChatResult(
            id = chatCommand.id,
            createdAt = OffsetDateTime.now(),
            model = transformerType.name.lowercase(),
            choices = listOf(
                ChatResult.Choice(
                    index = 0,
                    message = ChatResult.Message(
                        role = MessageRole.ASSISTANT,
                        content = output
                    ),
                    finishReason = if (totalTokens == step) {
                        FinishReason.LENGTH
                    } else {
                        FinishReason.STOP
                    }
                )
            ),
            usage = ChatResult.Usage(
                promptTokens = promptInfo.numPromptTokens,
                completionTokens = totalTokens.minus(promptInfo.numPromptTokens),
                totalTokens = totalTokens
            )
        )
    }

    private fun checkAndBuildPrompt(modelType: TransformerType, messages: List<ChatCommand.Message>): PromptToken {
        val promptAdapter = promptAdapterStrategies[modelType]
            ?: error("$modelType promptAdapter not load")
        val prompt = promptAdapter.buildPromptTemplate(messages)
        val promptTokens = IntArray(prompt.length + 3)
        val numPromptTokens = tokenizer.encode(vocabulary, prompt, 1, 0, promptTokens)
        require(numPromptTokens >= 1) {
            "Prompt token missing, expected at least 1 prompt token"
        }
        return PromptToken(promptTokens, numPromptTokens, prompt)
    }

    data class PromptToken(
        val promptTokens: IntArray,
        val numPromptTokens: Int,
        val prompt: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PromptToken

            if (!promptTokens.contentEquals(other.promptTokens)) return false
            if (numPromptTokens != other.numPromptTokens) return false
            if (prompt != other.prompt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = promptTokens.contentHashCode()
            result = 31 * result + numPromptTokens
            result = 31 * result + prompt.hashCode()
            return result
        }

    }

    override fun streamChat(chatCommand: ChatCommand): Sequence<StreamChatResult> {
        val transformerType = TransformerType.getByLowercase(chatCommand.model)
        val promptInfo = checkAndBuildPrompt(transformerType, chatCommand.messages)
        val step = minOf(chatCommand.maxTokens, modelWeight.config.seqLen)
        val tokens = generateTokens(transformerType, chatCommand, promptInfo, step)
        val modelName = transformerType.name.lowercase()
        return sequence {
            yield(
                createStreamChatResult(
                    chatCommand.id,
                    modelName,
                    MessageRole.ASSISTANT, ""
                )
            )
            val tokensCount = tokens.drop(promptInfo.numPromptTokens)
                .map { tokenizer.decode(vocabulary, it) }
                .fold(promptInfo.numPromptTokens) { count, content ->
                    yield(
                        createStreamChatResult(
                            chatCommand.id,
                            modelName,
                            null,
                            content,
                            null
                        )
                    )
                    count + 1
                }
            yield(
                createStreamChatResult(
                    chatCommand.id,
                    modelName,
                    null,
                    null,
                    if (tokensCount == step) FinishReason.LENGTH
                    else FinishReason.STOP
                )
            )
        }
    }

    private fun generateTokens(
        transformerType: TransformerType,
        chatCommand: ChatCommand,
        promptInfo: PromptToken,
        step: Int
    ): Sequence<Int> {
        val transformer = transformerStrategies[transformerType]
            ?: error("${chatCommand.model} transformer not load ")
        return transformer.iterativeGenerate(
            modelWeight,
            promptInfo.promptTokens,
            promptInfo.numPromptTokens,
            chatCommandMapper.toSampler(chatCommand, modelWeight.config.vocabSize),
            step
        )
    }

    fun createStreamChatResult(
        id: String,
        modelName: String,
        role: MessageRole?,
        content: String?,
        finishReason: FinishReason? = null
    ): StreamChatResult {
        return StreamChatResult(
            id = id,
            createdAt = OffsetDateTime.now(),
            model = modelName,
            choices = listOf(
                StreamChatResult.Choice(
                    index = 0,
                    delta = StreamChatResult.Delta(role = role, content = content),
                    finishReason = finishReason
                )
            )
        )
    }

}