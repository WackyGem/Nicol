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

import org.example.nicol.domain.inference.enums.TransformerType
import org.example.nicol.infrastructure.computation.VectorUtils
import org.example.nicol.infrastructure.entity.Sampler
import org.example.nicol.infrastructure.entity.RunState
import org.example.nicol.infrastructure.entity.ModelWeight
import org.springframework.stereotype.Service
import java.util.Arrays
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class Llama2 : Transformer {

    override val type: TransformerType
        get() = TransformerType.LLama2

    override fun iterativeGenerate(
        modelWeight:ModelWeight,
        promptTokens:IntArray,
        numPromptTokens:Int,
        sampler: Sampler,
        steps: Int
    ): Sequence<Int> =
        sequence {
            var next: Int
            var token = promptTokens[0]
            var pos = 0
            val runState = RunState(modelWeight.config)
            while (pos < steps) {
                val logits = forward(modelWeight, runState, token, pos)
                // Advance the state machine
                next = if (pos < numPromptTokens - 1) {
                    // If we are still processing the input prompt, force the next prompt token
                    promptTokens[pos + 1]
                } else {
                    // Otherwise sample the next token from the logits
                    Sampler.sample(sampler, logits)
                }
                pos++
                if (next == 1) {
                    break
                }
                yield(next)
                token = next
            }
        }

    /**
     * Performs the forward pass of the transformer model for inference.
     *
     * This function calculates the output [FloatArray] logits for a given input [token] sequence and [pos].
     * It uses the parameters provided in [modelWeight] and the current [state] to perform the computation.
     *
     * @since 0.1.0
     */
    private fun forward(modelWeight: ModelWeight, state: RunState, token: Int, pos: Int): FloatArray {
        val p = modelWeight.config
        val w = modelWeight.weight
        val x = state.x
        val dim = p.dim
        val kvDim = (p.dim * p.nKvHeads) / p.nHeads
        val kvMul = p.nHeads / p.nKvHeads
        val hiddenDim = p.hiddenDim
        val headSize = dim / p.nHeads
        // copy the token embedding into x
        System.arraycopy(w.tokenEmbeddingTable, token * dim, x, 0, dim)
        for (l in 0 until p.nLayers) {
            // attention rmsnorm
            VectorUtils.rmsNorm(state.xb, x, w.rmsAttWeight, l * dim, dim)
            // qkv matmuls for this position
            VectorUtils.matmul(state.q, state.xb, w.wq, l * dim * dim, dim, dim)
            VectorUtils.matmul(state.k, state.xb, w.wk, l * dim * kvDim, dim, dim)
            VectorUtils.matmul(state.v, state.xb, w.wv, l * dim * kvDim, dim, dim)
            // RoPE relative positional encoding: complex-valued rotate q and k in each head
            for (i in 0 until dim step 2) {
                val headDim = i % headSize
                val freq = 1.0f / 10000.0f.pow(headDim / headSize.toFloat())
                val value = pos * freq
                val fcr = cos(value)
                val fci = sin(value)
                val rotn = if (i < kvDim) 2 else 1
                for (v in 0 until rotn) {
                    val vec = if (v == 0) state.q else state.k
                    val v0 = vec[i]
                    val v1 = vec[i + 1]
                    vec[i] = v0 * fcr - v1 * fci
                    vec[i + 1] = v0 * fci + v1 * fcr
                }
            }
            // save key,value at this time step (pos) to our kv cache
            val loffset = l * p.seqLen * kvDim
            System.arraycopy(state.k, 0, state.keyCache, loffset + pos * dim, dim)
            System.arraycopy(state.v, 0, state.valueCache, loffset + pos * dim, dim)
            // multihead attention. iterate over all heads
            for (h in 0 until p.nHeads) {
                val qOffset = h * headSize
                val attentionOffset = h * p.seqLen
                // iterate over all timesteps, including the current one
                for (t in 0..pos) {
                    // get the key vector for this head and at this timestep
                    // float* k = state.key_cache + loff + t * dim + h * head_size;
                    val keyCacheOffset = loffset + t * dim + h * headSize
                    // calculate the attention score as the dot product of q and k
                    var score = 0.0f
                    for (i in 0 until headSize) {
                        score += state.q[qOffset + i] * state.keyCache[keyCacheOffset + i]
                    }
                    score /= sqrt(headSize.toDouble()).toFloat()
                    // save the score to the attention buffer
                    state.att[attentionOffset + t] = score
                }
                VectorUtils.softmax(state.att, attentionOffset, pos + 1)
                val xbOffset = h * headSize
                Arrays.fill(state.xb, xbOffset, xbOffset + headSize, 0.0f)
                for (t in 0..pos) {
                    val vOffset = (loffset + t * kvDim + (h / kvMul) * headSize)
                    // get the attention weight for this timestep
                    val a = state.att[attentionOffset + t]
                    // accumulate the weighted value inconfigto xb
                    for (i in 0 until headSize) {
                        val fl = state.valueCache[vOffset + i]
                        state.xb[xbOffset + i] += a * fl
                    }
                }
            }
            // final matmul to get the output of the attention
            VectorUtils.matmul(state.xb2, state.xb, w.wo, l * dim * dim, dim, dim)
            // residual connection back into x
            for (i in 0 until dim) {
                x[i] += state.xb2[i]
            }
            // ffn rmsnorm
            VectorUtils.rmsNorm(state.xb, x, w.rmsFfnWeight, l * dim, dim)
            // Now for FFN in PyTorch we have: self.w2(F.silu(self.w1(x)) * self.w3(x))
            // first calculate self.w1(x) and self.w3(x)
            VectorUtils.matmul(state.hb, state.xb, w.w1, l * dim * hiddenDim, dim, hiddenDim)
            VectorUtils.matmul(state.hb2, state.xb, w.w3, l * dim * hiddenDim, dim, hiddenDim)
            // SwiGLU non-linearity
            for (i in 0 until hiddenDim) {
                var value = state.hb[i]
                value *= 1.0f / (1.0f + exp(-value.toDouble()).toFloat())
                value *= state.hb2[i]
                state.hb[i] = value
            }
            // final matmul to get the output of the ffn
            VectorUtils.matmul(state.xb, state.hb, w.w2, l * dim * hiddenDim, hiddenDim, dim)
            // residual connection
            for (i in 0 until dim) {
                x[i] += state.xb[i]
            }
        }
        VectorUtils.rmsNorm(state.x, state.x, w.rmsFinalWeight, 0, dim)
        VectorUtils.matmul(state.logits, state.x, w.wcls, 0, dim, p.vocabSize)
        return state.logits
    }
}