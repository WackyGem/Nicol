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

package org.example.nicol.domain.system.model.command

data class SendEmailCommand(
    val senderAddress: String,
    val subject: String,
    val content: String,
    val useHtmlTemplate: Boolean,
    val sendFrom: String,
    val sendTo: Array<String>,
    val cc: Array<String>?,
    val bcc: Array<String>?,
    val attachFiles: Array<String>?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendEmailCommand

        if (senderAddress != other.senderAddress) return false
        if (subject != other.subject) return false
        if (content != other.content) return false
        if (useHtmlTemplate != other.useHtmlTemplate) return false
        if (sendFrom != other.sendFrom) return false
        if (!sendTo.contentEquals(other.sendTo)) return false
        if (cc != null) {
            if (other.cc == null) return false
            if (!cc.contentEquals(other.cc)) return false
        } else if (other.cc != null) return false
        if (bcc != null) {
            if (other.bcc == null) return false
            if (!bcc.contentEquals(other.bcc)) return false
        } else if (other.bcc != null) return false
        if (attachFiles != null) {
            if (other.attachFiles == null) return false
            if (!attachFiles.contentEquals(other.attachFiles)) return false
        } else if (other.attachFiles != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = senderAddress.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + useHtmlTemplate.hashCode()
        result = 31 * result + sendFrom.hashCode()
        result = 31 * result + sendTo.contentHashCode()
        result = 31 * result + (cc?.contentHashCode() ?: 0)
        result = 31 * result + (bcc?.contentHashCode() ?: 0)
        result = 31 * result + (attachFiles?.contentHashCode() ?: 0)
        return result
    }
}