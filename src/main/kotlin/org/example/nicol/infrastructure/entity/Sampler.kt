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

import org.example.nicol.infrastructure.computation.VectorUtils
import kotlin.random.Random

data class Sampler(
    val vocabSize: Int,
    val temperature: Float = 0.0f,
    val topp: Float = 0.9f,
    var rngState: Long =  System.currentTimeMillis() / 1000L
) {

    data class ProbIndex(val prob: Float, val index: Int)

    val probIndex: Array<ProbIndex?> = arrayOfNulls(vocabSize)

    companion object {

        private fun sampleTopp(probabilities: FloatArray, topp: Float, coin: Float): Int {
            val n = probabilities.size
            val probindex = mutableListOf<ProbIndex>()
            val cutoff = (1.0f - topp) / (n - 1).toFloat()
            var n0 = 0
            for (i in 0 until n) {
                if (probabilities[i] >= cutoff) {
                    probindex.add(ProbIndex(probabilities[i], i))
                    n0++
                }
            }
            probindex.sortByDescending { it.prob }
            var cumulativeProb = 0.0f
            var lastIdx = n0 - 1
            for (i in 0 until probindex.size) {
                cumulativeProb += probindex[i].prob
                if (cumulativeProb > topp) {
                    lastIdx = i
                    break
                }
            }

            val r = coin * cumulativeProb
            var cdf = 0.0f
            for (i in 0..lastIdx) {
                cdf += probindex[i].prob
                if (r < cdf) {
                    return probindex[i].index
                }
            }
            return probindex[lastIdx].index
        }

        fun sample(sampler: Sampler, logits : FloatArray) :Int {
            val next:Int = if(sampler.temperature == 0.0f) {
                VectorUtils.argmax(logits)
            }else {
                val scaledLogits = logits.map { it / sampler.temperature }.toFloatArray()
                VectorUtils.softmax(scaledLogits,0, sampler.vocabSize)
                val coin = Random(sampler.rngState).nextFloat()
                if (sampler.topp <= 0 || sampler.topp >= 1) {
                    VectorUtils.mult(scaledLogits, coin)
                }else{
                    sampleTopp(scaledLogits, sampler.topp, coin)
                }
            }
            return next;
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sampler

        if (vocabSize != other.vocabSize) return false
        if (temperature != other.temperature) return false
        if (!probIndex.contentEquals(other.probIndex)) return false
        if (topp != other.topp) return false
        if (rngState != other.rngState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vocabSize
        result = 31 * result + temperature.hashCode()
        result = 31 * result + probIndex.contentHashCode()
        result = 31 * result + topp.hashCode()
        result = 31 * result + rngState.hashCode()
        return result
    }
}