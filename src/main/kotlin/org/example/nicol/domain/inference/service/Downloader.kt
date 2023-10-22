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

import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestTemplate
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.util.Arrays


interface Downloader {

    fun checkAndDownloadFile(directoryPath: String, targetFileName: String, defaultDownloadUrl: String)
}

@Service
class ModelDownloader(val restTemplate: RestTemplate) : Downloader {

    private val logger = LoggerFactory.getLogger(ModelDownloader::class.java)

    override fun checkAndDownloadFile(directoryPath: String, targetFileName: String, defaultDownloadUrl: String) {
        val targetFile = File("$directoryPath/$targetFileName")
        val parentDirectory = targetFile.parentFile
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        if (!targetFile.exists()) {
            logger.info("Could not found {}, Download starting...",targetFileName)
            val requestCallback = RequestCallback { request: ClientHttpRequest ->
                request.headers.accept = Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL)
            }
            val responseExtractor = ResponseExtractor<Void> { response: ClientHttpResponse ->
                Files.copy(response.body, targetFile.toPath())
                null
            }
            restTemplate.execute(URI.create(defaultDownloadUrl), HttpMethod.GET, requestCallback, responseExtractor)
            logger.info("Download {} completed.",targetFileName)
        }
    }

}