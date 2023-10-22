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

package org.example.nicol.infrastructure.entity

import org.example.nicol.application.mq.callback.SendVerifyEmailCallback
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Vocabulary(tokenizerPath:String, transWeight: ModelWeight) {

    private val logger = LoggerFactory.getLogger(Vocabulary::class.java)

    val vocab = Array(transWeight.config.vocabSize) { "" }

    val vocabScores = FloatArray(transWeight.config.vocabSize)

    private val maxTokenLength: Int;

    val bytePieces = CharArray(256 * 2)

    val sortedVocab: Array<TokenIndex>

    data class TokenIndex(val str: String, val id: Int)

    init {
        logger.info("Initializing Vocabulary starting...")
        FileChannel.open(Paths.get(tokenizerPath), StandardOpenOption.READ).use { channel ->
            val bb: ByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
            bb.order(ByteOrder.LITTLE_ENDIAN)
            maxTokenLength = bb.getInt()
            for (i in 0 until transWeight.config.vocabSize) {
                vocabScores[i] = bb.getFloat()
                val len = bb.getInt()
                val bytes = ByteArray(len)
                bb[bytes]
                vocab[i] = String(bytes,Charsets.UTF_8)
            }
            for (i in 0 until 256) {
                bytePieces[i * 2] = i.toChar()
                bytePieces[i * 2 + 1] = 0.toChar();
            }
        }

        sortedVocab = Array(transWeight.config.vocabSize) { i ->
            TokenIndex(
                vocab[i],
                i
            )
        }
        sortedVocab.sortWith(::compareTokens)
        logger.info("Initializing Vocabulary completed.")
    }

    companion object {
        fun compareTokens(a: TokenIndex, b: TokenIndex): Int {
            return a.str.compareTo(b.str)
        }

        fun strLookup(str: String, sortedVocab: Array<TokenIndex>): Int {
            val tok = TokenIndex(str, 0)
            val resIndex = sortedVocab.binarySearch(tok, ::compareTokens)
            return if (resIndex >= 0) sortedVocab[resIndex].id else -1
        }
    }
}