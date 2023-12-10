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
import org.example.nicol.infrastructure.enhance.strategy.StrategyKey
import org.example.nicol.infrastructure.entity.Sampler
import org.example.nicol.infrastructure.entity.ModelWeight

interface Transformer {

    @get:StrategyKey
    val type:TransformerType

    /**
     * One by one generates a sequence of text based on the provided transformer model.
     *
     * This function uses the transformer weight defined by [modelWeight] to generate a sequence of text.
     * It takes an optional [prompt] string as input and generates text for a specified number of [steps].
     *
     * @since 0.1.0
     */
    fun iterativeGenerate(
        modelWeight:ModelWeight,
        promptTokens:IntArray,
        numPromptTokens:Int,
        sampler: Sampler,
        steps: Int
    ): Sequence<Int>



}