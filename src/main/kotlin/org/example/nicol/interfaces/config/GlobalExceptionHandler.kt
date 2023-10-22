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

package org.example.nicol.interfaces.config

import org.example.nicol.domain.system.exception.BadCredentialsException
import org.example.nicol.domain.system.exception.IncorrectSessionException
import org.example.nicol.domain.system.exception.VerifyEmailExpiredException
import org.example.nicol.interfaces.enums.ErrorCode
import org.example.nicol.interfaces.payload.response.ErrorResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = ResponseEntity(
        ErrorResponse(ErrorCode.FORBIDDEN, ex.message),
        HttpStatus.BAD_REQUEST
    )

    @ExceptionHandler(IncorrectSessionException::class)
    fun handleSessionException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = ResponseEntity(
        ErrorResponse(ErrorCode.SESSION_FAIL, ex.message),
        HttpStatus.UNAUTHORIZED
    )

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        VerifyEmailExpiredException::class
    )
    fun handleExpiredSerialException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = ResponseEntity(
        ErrorResponse(ErrorCode.INVALID_PARAMETER, ex.message),
        HttpStatus.BAD_REQUEST
    )

    @ExceptionHandler(Exception::class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun handleInternalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> = ResponseEntity(
        ErrorResponse(ErrorCode.UNKNOWABLE, ex.message ?: "Unknown error"),
        HttpStatus.INTERNAL_SERVER_ERROR
    )

}