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

/** A structure used to cache the current state of Attention activation */
data class RunState(val config: ModelWeight.Config) {
    // activation at current time stamp (dim,)
    val x = FloatArray(config.dim)

    //  activation inside a residual branch
    val xb = FloatArray(config.dim)

    // an additional buffer just for convenience (dim,)
    val xb2 = FloatArray(config.dim)

    // buffer for hidden dimension in the ffn (hidden_dim,)
    val hb = FloatArray(config.hiddenDim)

    // buffer for hidden dimension in the ffn (hidden_dim,)
    val hb2 = FloatArray(config.hiddenDim)

    // query (dim,)
    val q = FloatArray(config.dim)

    // key (dim,)
    val k = FloatArray(config.dim)

    // value (dim,)
    val v = FloatArray(config.dim)

    // buffer for scores/attention values (n_heads, seq_len)
    val att = FloatArray(config.nHeads * config.seqLen)

    // output logits
    val logits = FloatArray(config.vocabSize)

    // kv cache
    val keyCache = FloatArray(config.nLayers * config.seqLen * config.dim)
    val valueCache = FloatArray(config.nLayers * config.seqLen * config.dim)
}
