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
import org.example.nicol.infrastructure.entity.Vocabulary
import org.springframework.stereotype.Service

@Service
class UTF8Tokenizer : Tokenizer {

    override val type: TokenizerType
        get() = TokenizerType.UTF8Tokenizer

    override fun encode(
        vocabulary: Vocabulary,
        text: String,
        bos: Byte,
        eos: Byte,
        tokens: IntArray
    ): Int {

        // create a temporary buffer that will store merge candidates of always two consecutive tokens
        // *2 for concat, +1 for null terminator +2 for UTF8 (in case maxTokenLength is 1)
        var strLen = 0

        // start at 0 tokens
        var nTokens = 0

        // add optional BOS (=1) token, if desired
        if (bos.toInt() == 1) {
            tokens[nTokens++] = 1
        }

        // add dummy prefix if text is not empty
        if (text.isNotEmpty()) {
            val dummyPrefix = Vocabulary.strLookup(" ", vocabulary.sortedVocab)
            tokens[nTokens++] = dummyPrefix
        }

        // UTF-8 processing
        for (c in text) {
            if ((c.code and 0xC0) != 0x80) {
                strLen = 0
            }
            strLen++;


            val id = Vocabulary.strLookup(c.toString(), vocabulary.sortedVocab)

            if (id != -1) {
                tokens[nTokens++] = id
            } else {
                for (i in 0 until strLen) {
                    tokens[nTokens++] = c.code + 3
                }
            }
            strLen = 0
        }

        // merge consecutive tokens
        while (true) {
            var bestScore = -1e10
            var bestId = -1
            var bestIdx = -1

            for (i in 0 until nTokens - 1) {
                val str = "${vocabulary.vocab[tokens[i]]}${vocabulary.vocab[tokens[i + 1]]}"
                val id = Vocabulary.strLookup(str, vocabulary.sortedVocab)
                if (id != -1 && vocabulary.vocabScores[id] > bestScore) {
                    bestScore = vocabulary.vocabScores[id].toDouble()
                    bestId = id
                    bestIdx = i
                }
            }

            if (bestIdx == -1) {
                break
            }

            tokens[bestIdx] = bestId
            for (i in bestIdx + 1 until nTokens - 1) {
                tokens[i] = tokens[i + 1]
            }
            nTokens--
        }

        // add optional EOS (=2) token, if desired
        if (eos.toInt() != 0) {
            tokens[nTokens++] = 2
        }
        return nTokens
    }

    override fun decode(vocabulary: Vocabulary, token: Int): String {
        val inputString = vocabulary.vocab[token]
        return "<0x([0-9A-Fa-f]{2})>".toRegex().replace(inputString) { result ->
            val hexValue = result.groupValues[1]
            val byteVal = hexValue.toInt(16)

            if (byteVal < vocabulary.bytePieces.size) {
                vocabulary.bytePieces[byteVal * 2].toString()
            } else {
                inputString
            }
        }
    }

}