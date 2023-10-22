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

import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.math.abs

class ModelWeight(checkPointPath:String) {

    private val logger = LoggerFactory.getLogger(Vocabulary::class.java)

    val config: Config

    val weight: Weight

    init {
        logger.info("Initializing ModelWeight starting...")
        FileChannel.open(Paths.get(checkPointPath), StandardOpenOption.READ).use { fileChannel ->
            val byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            config = Config.from(byteBuffer)
            weight = Weight.from(config, byteBuffer.asFloatBuffer())
        }
        logger.info("Initializing ModelWeight completed.")
    }

    data class Config(
        // transformer dimension
        val dim: Int,
        // for ffn layers
        val hiddenDim: Int,
        // number of layers
        val nLayers: Int,
        // number of query heads
        val nHeads: Int,
        // number of key/value heads (can be < query heads because of multiquery)
        val nKvHeads: Int,
        // vocabulary size, usually 256 (byte-level)
        val vocabSize: Int,
        // max sequence length
        val seqLen: Int,
    ) {
        val sharedWeights: Boolean = vocabSize > 0

        companion object {
            fun from(buffer: ByteBuffer): Config {
                val dim = buffer.int
                val hiddenDim = buffer.int
                val nLayers = buffer.int
                val nHeads = buffer.int
                val nKvHeads = buffer.int
                var vocabSize = buffer.int
                vocabSize = abs(vocabSize)
                val seqLen = buffer.int
                return Config(dim, hiddenDim, nLayers, nHeads, nKvHeads, vocabSize, seqLen)
            }
        }
    }

    data class Weight(
        // token embedding table (vocab_size, dim)
        val tokenEmbeddingTable: FloatArray,
        // (layer, dim) rmsnorm weights
        val rmsAttWeight: FloatArray,
        // (layer, dim)
        val rmsFfnWeight: FloatArray,
        // weights for matmuls. note dim == n_heads * head_size (layer, dim, n_heads * head_size)
        val wq: FloatArray,
        // (layer, dim, n_heads * head_size)
        val wk: FloatArray,
        // (layer, dim, n_kv_heads * head_size)
        val wv: FloatArray,
        // (layer, n_heads * head_size, dim)
        val wo: FloatArray,
        // (layer, hidden_dim, dim)
        val w1: FloatArray,
        // (layer, dim, hidden_dim)
        val w2: FloatArray,
        // (layer, hidden_dim, dim)
        val w3: FloatArray,
        // final rmsnorm (dim,)
        val rmsFinalWeight: FloatArray,
        // (optional) classifier weights for the logits, on the last layer
        val wcls: FloatArray,
    ) {
        companion object {
            fun from(config: Config, buffer: FloatBuffer): Weight {
                val tokenEmbeddingTable = array(buffer, config.vocabSize * config.dim)
                return Weight(
                    tokenEmbeddingTable = tokenEmbeddingTable,
                    rmsAttWeight = array(buffer, config.nLayers * config.dim),
                    wq = array(buffer, config.nLayers * config.dim * config.dim),
                    wk = array(buffer, config.nLayers * config.dim * config.dim),
                    wv = array(buffer, config.nLayers * config.dim * config.dim),
                    wo = array(buffer, config.nLayers * config.dim * config.dim),
                    rmsFfnWeight = array(buffer, config.nLayers * config.dim),
                    w1 = array(buffer, config.nLayers * config.hiddenDim * config.dim),
                    w2 = array(buffer, config.nLayers * config.dim * config.hiddenDim),
                    w3 = array(buffer, config.nLayers * config.hiddenDim * config.dim),
                    rmsFinalWeight = array(buffer, config.dim),
                    wcls = if (config.sharedWeights) tokenEmbeddingTable else FloatArray(0)
                )
            }

            private fun array(buffer: FloatBuffer, size: Int): FloatArray {
                val floats = FloatArray(size)
                buffer[floats]
                return floats
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Weight

            if (!tokenEmbeddingTable.contentEquals(other.tokenEmbeddingTable)) return false
            if (!rmsAttWeight.contentEquals(other.rmsAttWeight)) return false
            if (!rmsFfnWeight.contentEquals(other.rmsFfnWeight)) return false
            if (!wq.contentEquals(other.wq)) return false
            if (!wk.contentEquals(other.wk)) return false
            if (!wv.contentEquals(other.wv)) return false
            if (!wo.contentEquals(other.wo)) return false
            if (!w1.contentEquals(other.w1)) return false
            if (!w2.contentEquals(other.w2)) return false
            if (!w3.contentEquals(other.w3)) return false
            if (!rmsFinalWeight.contentEquals(other.rmsFinalWeight)) return false
            if (!wcls.contentEquals(other.wcls)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tokenEmbeddingTable.contentHashCode()
            result = 31 * result + rmsAttWeight.contentHashCode()
            result = 31 * result + rmsFfnWeight.contentHashCode()
            result = 31 * result + wq.contentHashCode()
            result = 31 * result + wk.contentHashCode()
            result = 31 * result + wv.contentHashCode()
            result = 31 * result + wo.contentHashCode()
            result = 31 * result + w1.contentHashCode()
            result = 31 * result + w2.contentHashCode()
            result = 31 * result + w3.contentHashCode()
            result = 31 * result + rmsFinalWeight.contentHashCode()
            result = 31 * result + wcls.contentHashCode()
            return result
        }
    }

}

