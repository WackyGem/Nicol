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

package org.example.nicol.application

import org.example.nicol.domain.system.exception.TokenExpiredException
import org.example.nicol.domain.system.model.command.CreateUserCommand
import org.example.nicol.domain.system.model.command.LoginUserCommand
import org.example.nicol.domain.system.model.command.RecordVerifyEmailCommand
import org.example.nicol.domain.system.model.command.CheckAccessTokenCommand
import org.example.nicol.domain.system.model.command.GetUserAuthCommand
import org.example.nicol.domain.system.model.command.VerifyEmailCommand
import org.example.nicol.domain.system.model.result.LoginUserResult
import org.example.nicol.domain.system.model.result.RenewAccessTokenResult
import org.example.nicol.domain.system.model.result.UserResult
import org.example.nicol.domain.system.model.result.UserVerifyEmailResult
import org.example.nicol.domain.system.model.result.VerifyEmailResult
import org.example.nicol.domain.system.model.vo.Payload
import org.example.nicol.domain.system.model.vo.UserAuth

interface UserService {
    /** register user */
    fun registerUser(createUserCommand: CreateUserCommand): UserResult

    /** login */
    fun loginUser(loginUserCommand: LoginUserCommand): LoginUserResult

    /** renew token */
    fun renewToken(checkTokenCommand: CheckAccessTokenCommand): RenewAccessTokenResult

    /** verify email */
    fun verifyEmail(verifyEmailCommand: VerifyEmailCommand): UserVerifyEmailResult

    /** record verify email */
    fun recordVerifyEmail(recordVerifyEmailCommand: RecordVerifyEmailCommand):VerifyEmailResult

    /** check Token */
    @Throws(TokenExpiredException::class)
    fun checkToken(checkTokenCommand: CheckAccessTokenCommand): Payload

    /** get [UserAuth] which implement [org.springframework.security.core.userdetails.UserDetails] */
    fun getUserAuth(getUserAuthCommand: GetUserAuthCommand):UserAuth
}