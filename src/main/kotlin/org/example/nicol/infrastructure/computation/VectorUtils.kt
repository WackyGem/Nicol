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

package org.example.nicol.infrastructure.computation

import kotlin.math.exp
import kotlin.math.sqrt

object VectorUtils {

    /**
     * This function calculates the RMS normalized vector output by applying the following steps:
     *
     * 1. Calculate the sum of squares ss of the input vector input:
     *      ss = ∑_{j=0}^{size-1} (x[[j]])^2
     *
     * 2. Normalize the sum of squares ss :
     *      ss = ss/size + 1 * 10^(-5)
     *      ss = 1/√ss
     *
     * 3. Normalize and scale the output vector output:
     *      o[[j]] = weight[[j]] * (ss * x[[j]])
     *
     * @param output The output vector.
     * @param input The input vector.
     * @param weight The weight vector.
     * @param offset The offset of weight vector
     * @param size The size of the vectors.
     */
    fun rmsNorm(output: FloatArray, input: FloatArray, weight: FloatArray, offset: Int, size: Int) {
        var ss = 0.0f
        for (j in 0 until size) {
            ss += input[j] * input[j]
        }
        ss /= size.toFloat()
        ss += 1e-5f
        ss = 1.0f / sqrt(ss.toDouble()).toFloat()
        for (j in 0 until size) {
            output[j] = weight[offset + j] * (ss * input[j])
        }
    }

    /**
     * This function calculates the softmax normalized vector output by applying the following steps:
     *
     * 1. Find the maximum value maxVal in the input vector x:
     *      maxVal = max(x[0], x[1], ..., x[size-1])
     *
     * 2. Compute the exponential of the differences between each element and the maximum value:
     *      exp_diff[i] = e^(x[i] - maxVal)
     *
     * 3. Calculate the sum of the exponential differences:
     *      sum = ∑_{i=0}^{size-1} exp_diff[i]
     *
     * 4. Normalize each element in the output vector x by dividing them by the sum:
     *      x[i] = exp_diff[i] / sum
     *
     * @param x The input vector containing unnormalized values.
     * @param size The size of the vector.
     */
    fun softmax(x: FloatArray, offset: Int, size: Int) {
        var maxVal = x[0 + offset]
        for (i in 1 until size) {
            if (x[i + offset] > maxVal) {
                maxVal = x[i + offset]
            }
        }
        var sum = 0.0f
        for (i in 0 until size) {
            x[i + offset] = exp((x[i + offset] - maxVal).toDouble()).toFloat()
            sum += x[i + offset]
        }
        for (i in 0 until size) {
            x[i + offset] /= sum
        }
    }

    /**
     * This function performs matrix multiplication between a weight matrix and an input vector,
     * resulting in an output vector. It follows these steps:
     *
     * 1. For each row of the weight matrix and corresponding element in the output vector:
     *    1.1. Initialize the value for the current element in the output vector as 0.0.
     *    1.2. Multiply each element in the row of the weight matrix with the corresponding element in the input vector.
     *    1.3. Accumulate the products to calculate the value for the current element in the output vector.
     *
     * @param output The output vector after matrix multiplication.
     * @param input The input vector to be multiplied.
     * @param weight The weight matrix for multiplication.
     * @param offset The offset of weight vector
     * @param col The number of columns in the weight matrix and size of the input vector.
     * @param row The number of rows in the weight matrix and size of the output vector.
     */
    fun matmul(output: FloatArray, input: FloatArray, weight: FloatArray, offset: Int, col: Int, row: Int) {
        // W (d,n) @ x (n,) -> xout (d,)
        // by far the most amount of time is spent inside this little function
        for (i in 0 until row) {
            var value = 0.0f
            for (j in 0 until col) {
                value += weight[offset + (i * col + j)] * input[j]
            }
            output[i] = value
        }
    }

    /** Find a max index with [probabilities] vector */
    fun argmax(probabilities: FloatArray): Int {
        var maxIndex = 0
        var maxProbability = probabilities[0]

        for (i in 1 until probabilities.size) {
            if (probabilities[i] > maxProbability) {
                maxIndex = i
                maxProbability = probabilities[i]
            }
        }

        return maxIndex
    }

    /**
     * A multinomial distribution sampling algorithm
     *
     * Randomly samples an index from a [probability] distribution
     * based on a given array of probabilities and a random number [coin]
     */
    fun mult(probabilities: FloatArray, coin: Float): Int {
        var cdf = 0.0f
        for (i in probabilities.indices) {
            cdf += probabilities[i]
            if (coin < cdf) {
                return i
            }
        }
        return probabilities.size - 1
    }
}