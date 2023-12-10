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

import org.example.nicol.domain.inference.enums.TokenizerType
import org.example.nicol.infrastructure.enhance.strategy.StrategyKey
import org.example.nicol.infrastructure.entity.Vocabulary

interface Tokenizer {

    @get:StrategyKey
    val type: TokenizerType

    /**
     * Encodes a [text] sequence into an array of integer [tokens] using the provided [vocabulary].
     *
     * This function takes a [text] input and encodes it into a sequence of integer [tokens]
     * based on the given [vocabulary].
     * It also supports the addition of optional beginning-of-sequence [bos] and end-of-sequence [eos] tokens.
     *
     * @since 0.1.0
     */
    fun encode(vocabulary: Vocabulary, text: String, bos: Byte, eos: Byte, tokens: IntArray): Int

    /**
     * Decodes an integer token into a text piece using the provided [vocabulary].
     *
     * @since 0.1.0
     */
    fun decode(vocabulary: Vocabulary,token: Int): String
}