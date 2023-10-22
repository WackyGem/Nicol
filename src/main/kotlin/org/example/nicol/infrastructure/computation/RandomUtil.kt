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

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Random

inline fun <reified T : Any> randomValue(length: Int = 0): T {
    val random = Random()
    return when (T::class) {
        Int::class -> random.nextInt(length) as T
        Long::class -> random.nextLong(length.toLong()) as T
        Float::class -> random.nextFloat(length.toFloat()) as T
        Char::class -> {
            require(length > 0) { "Length must be greater than 0 for Char type" }
            val charPool = ('a'..'z') + ('A'..'Z')
            val randomString = (1..length)
                .map { random.nextInt(charPool.size) }
                .map(charPool::get)
                .joinToString("")
            randomString.first() as T
        }
        String::class -> {
            require(length > 0) { "Length must be greater than 0 for String type" }
            val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            (1..length)
                .map { random.nextInt(charPool.size) }
                .map(charPool::get)
                .joinToString("")
                    as T
        }
        LocalDate::class -> LocalDate.of(
            random.nextInt(1900, 2100),
            random.nextInt(1, 13),
            random.nextInt(1, 29)
        ) as T
        LocalDateTime::class -> LocalDateTime.of(
            random.nextInt(1900, 2100),
            random.nextInt(1, 13),
            random.nextInt(1, 29),
            random.nextInt(0, 24),
            random.nextInt(0, 60)
        ) as T
        OffsetDateTime::class -> {
            val year = random.nextInt(1900, 2100)
            val month = random.nextInt(1, 13)
            val dayOfMonth = random.nextInt(1, 29)
            val hour = random.nextInt(0, 24)
            val minute = random.nextInt(0, 60)
            val second = random.nextInt(0, 60)
            val nano = random.nextInt(0, 1_000_000)
            val offset = ZoneOffset.ofTotalSeconds(random.nextInt(-18 * 3600, 18 * 3600)) // Random offset
            OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nano, offset) as T
        }
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
    }

}

fun randomEmail(): String {
    val name = randomValue<String>(8)
    val domains = listOf("gmail.com", "yahoo.com", "outlook.com")
    val domain = domains[randomValue<Int>(domains.size)]
    return "$name@$domain"
}
