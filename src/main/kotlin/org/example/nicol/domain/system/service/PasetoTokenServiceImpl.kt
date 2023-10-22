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

package org.example.nicol.domain.system.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.nicol.domain.system.exception.TokenExpiredException
import org.example.nicol.domain.system.model.vo.Payload
import org.paseto4j.commons.SecretKey
import org.paseto4j.commons.Version
import org.paseto4j.version3.Paseto
import org.springframework.stereotype.Service
import java.time.OffsetDateTime


@Service
class PasetoTokenServiceImpl(
    val objectMapper: ObjectMapper
) : TokenService {

    override fun createToken(payload: Payload, tokenSymmetricKey: String, footer: String?): Pair<String, Payload> =
        Pair(
            Paseto.encrypt(
                SecretKey(
                    tokenSymmetricKey.toByteArray(), Version.V3
                ), objectMapper.writeValueAsString(payload), footer
            ), payload
        )

    override fun verifyToken(token: String, tokenSymmetricKey: String, footer: String?): Payload =
        objectMapper.readValue(
            Paseto.decrypt(
                SecretKey(
                    tokenSymmetricKey.toByteArray(), Version.V3
                ), token, footer
            ), Payload::class.java
        ).takeIf { it.expiredAt.isAfter(OffsetDateTime.now()) }
            ?: throw TokenExpiredException("The token was expired")

}