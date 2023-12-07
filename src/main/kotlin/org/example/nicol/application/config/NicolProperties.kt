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

package org.example.nicol.application.config

import org.example.nicol.application.mq.callback.CallbackFunctionType
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("nicol")
data class NicolProperties(
    val security: SecurityProperties,
    val inference: InferenceProperties,
    val mq: RedisMqProperties,
    val mail: MailProperties,
    val httpProxy: HttpProxyProperties
) {

    data class HttpProxyProperties(
        val enabled:Boolean,
        val host:String,
        val port:Int
    )

    data class SecurityProperties(
        val enabled: Boolean,
        val accessTokenDuration: Duration,
        val refreshTokenDuration: Duration,
        val tokenSymmetricKey: String,
        val footer: String
    )

    data class InferenceProperties(
        val driverMode: DriverMode,
        val model: ModelProperties,
        val tokenizer: TokenizerProperties
    ) {
        data class ModelProperties(val checkPoint: String)

        data class TokenizerProperties(val path: String)
    }

    enum class DriverMode {
        GPU,
        CPU
    }

    data class RedisMqProperties(val streams: List<MqStreamProperties>)

    data class MqStreamProperties(val topic: String, val groups: List<MqGroupProperties>) {

        data class MqGroupProperties(
            val name: String,
            val callback: CallbackFunctionType,
            val consumers: List<String>
        )

    }

    data class MailProperties(
        val sender: String,
        val topic: String,
        val callbackUrl: String,
        val subject: String
    )

}