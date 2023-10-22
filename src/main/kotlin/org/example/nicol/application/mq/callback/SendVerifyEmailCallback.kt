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

package org.example.nicol.application.mq.callback

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.example.nicol.application.UserService
import org.example.nicol.application.config.NicolProperties
import org.example.nicol.application.mq.callback.SendVerifyEmailCallback.Companion.VERIFY_EMAIL_TEMPLATE
import org.example.nicol.domain.system.model.command.SendEmailCommand
import org.example.nicol.domain.system.model.mapper.UserMapper
import org.example.nicol.domain.system.model.result.UserResult
import org.example.nicol.domain.system.model.result.VerifyEmailResult
import org.example.nicol.domain.system.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Component
class SendVerifyEmailCallback(
    val templateEngine: TemplateEngine,
    val emailService: EmailService,
    val objectMapper: ObjectMapper,
    val mailProperties: MailProperties,
    val nicolProperties: NicolProperties,
    val userService: UserService,
    val userMapper: UserMapper
) {
    private val logger = LoggerFactory.getLogger(SendVerifyEmailCallback::class.java)

    companion object {
        const val VERIFY_EMAIL_TEMPLATE = "verify-mail"
    }

    @PostConstruct
    fun initialize() {
        CallBackFunctionRegistry.register(
            CallbackFunctionType.SEND_VERIFY_EMAIL, this::sendVerifyEmail
        )
    }

    private fun sendVerifyEmail(record: MapRecord<String, String, String>) {
        val user = objectMapper.convertValue(record.value, UserResult::class.java)
        val verifyEmail = userService.recordVerifyEmail(userMapper.toRecordVerifyEmailCommand(user))
        sendVerifyEmailAfterCheck(verifyEmail, user.username)
        logger.info(
            "user: {} , email: {}  verify email has been send.",
            user.username,
            user.email
        )
    }

    private fun sendVerifyEmailAfterCheck(verifyEmail: VerifyEmailResult, username: String) =
        emailService.sendEmail(
            SendEmailCommand(
                senderAddress = mailProperties.username,
                subject = nicolProperties.mail.subject,
                content = buildContext(nicolProperties.mail.subject, username, verifyEmail),
                useHtmlTemplate = true,
                sendFrom = nicolProperties.mail.sender,
                sendTo = arrayOf(verifyEmail.email),
                cc = emptyArray(),
                bcc = emptyArray(),
                attachFiles = emptyArray()
            )
        )

    private fun buildContext(
        subject: String,
        username: String,
        verifyEmail: VerifyEmailResult
    ): String {
        val context = Context()
        context.setVariable("subject", subject)
        context.setVariable("username", username)
        context.setVariable(
            "callbackUrl",
            nicolProperties.mail.callbackUrl +
                    "/${verifyEmail.id}" +
                    "/${verifyEmail.secretCode}"
        )
        return templateEngine.process(VERIFY_EMAIL_TEMPLATE, context)
    }
}