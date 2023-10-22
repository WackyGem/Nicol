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

import org.example.nicol.application.mq.worker.DefaultStreamListener
import org.example.nicol.domain.system.model.command.SendEmailCommand
import org.slf4j.LoggerFactory
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailServiceImpl(
    val mailSender: JavaMailSender,
) : EmailService {
    private val logger = LoggerFactory.getLogger(EmailServiceImpl::class.java)

    override fun sendEmail(sendEmailCommand: SendEmailCommand) {
        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)
        helper.setFrom(sendEmailCommand.senderAddress,sendEmailCommand.sendFrom)
        helper.setTo(sendEmailCommand.sendTo)
        sendEmailCommand.cc?.let { helper.setCc(it) }
        sendEmailCommand.bcc?.let { helper.setBcc(it) }
        helper.setSubject(sendEmailCommand.subject)
        helper.setText(sendEmailCommand.content,sendEmailCommand.useHtmlTemplate)
        try {
            mailSender.send(mimeMessage)
        }catch (e: MailException){
            logger.error("send email to {} has been error, cause for {}",sendEmailCommand.sendTo,e.message)
        }
    }
}