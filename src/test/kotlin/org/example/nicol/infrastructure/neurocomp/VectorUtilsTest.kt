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

package org.example.nicol.infrastructure.neurocomp

import org.example.nicol.infrastructure.computation.VectorUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class VectorUtilsTest {

    @Test
    fun test_root_mean_square_norm_normal_case() {
        val input = floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
        val output = FloatArray(input.size)
        val weight = floatArrayOf(0.5f, 0.4f, 0.3f, 0.2f, 0.1f)
        val size = input.size
        VectorUtils.rmsNorm(output, input, weight, 0, size)
        val expectedOutput: FloatArray = floatArrayOf(0.1507556f, 0.24120896f, 0.2713601f, 0.24120896f, 0.1507556f)
        assertCloseEnough(expectedOutput, output)
    }

    @Test
    fun test_softmax_normal_case() {
        val input = floatArrayOf(1.0f, 2.0f, 3.0f)
        val size = input.size
        val expectedOutput = floatArrayOf(0.09003057f, 0.24472848f, 0.66524094f)
        VectorUtils.softmax(input, 0,size)
        assertCloseEnough(expectedOutput, input)
    }

    @Test
    fun test_matrix_multiplication_normal_case() {
        val input = floatArrayOf(1.0f, 2.0f, 3.0f)
        val weight = floatArrayOf(
            2.0f, 1.0f, 3.0f,
            0.5f, 1.5f, 2.0f
        )
        val row = 2
        val col = 3
        val output = FloatArray(row)
        val expectedOutput = floatArrayOf(
            13.0f,
            9.5f
        )
        VectorUtils.matmul(output, input, weight, 0, col, row)
        assertCloseEnough(expectedOutput, output)
    }

    private fun assertCloseEnough(expectedOutput: FloatArray, input: FloatArray) {
        for (i in expectedOutput.indices) {
            assertThat(input[i].toDouble(), `is`(closeTo(expectedOutput[i].toDouble(), 1e-6)))
        }
    }

}