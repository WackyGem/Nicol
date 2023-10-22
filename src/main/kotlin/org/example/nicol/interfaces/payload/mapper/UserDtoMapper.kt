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

package org.example.nicol.interfaces.payload.mapper

import org.example.nicol.domain.system.model.command.CheckAccessTokenCommand
import org.example.nicol.domain.system.model.command.LoginUserCommand
import org.example.nicol.domain.system.model.command.CreateUserCommand
import org.example.nicol.domain.system.model.result.LoginUserResult
import org.example.nicol.domain.system.model.result.RenewAccessTokenResult
import org.example.nicol.domain.system.model.result.UserResult
import org.example.nicol.domain.system.model.result.UserVerifyEmailResult
import org.example.nicol.interfaces.payload.request.CreateUserRequest
import org.example.nicol.interfaces.payload.request.LoginUserRequest
import org.example.nicol.interfaces.payload.request.RenewAccessTokenRequest
import org.example.nicol.interfaces.payload.response.LoginUserResponse
import org.example.nicol.interfaces.payload.response.RenewAccessTokenResponse
import org.example.nicol.interfaces.payload.response.UserResponse
import org.example.nicol.interfaces.payload.response.UserVerifyEmailResponse
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "Spring",imports = [java.time.OffsetDateTime::class, java.util.UUID::class])
interface UserDtoMapper {
    fun toCreateUserCommand(createUserRequest: CreateUserRequest):CreateUserCommand

    fun toUserResponse(userResult: UserResult): UserResponse

    fun toLoginUserCommand(loginUserRequest: LoginUserRequest): LoginUserCommand

    fun toLoginUserResponse(loginUser: LoginUserResult): LoginUserResponse

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toRenewAccessTokenCommand(renewAccessTokenRequest: RenewAccessTokenRequest): CheckAccessTokenCommand

    fun toRenewAccessTokenResponse(renewAccessTokenResult: RenewAccessTokenResult): RenewAccessTokenResponse

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toUserVerifyEmailResponse(userVerifyEmailResult: UserVerifyEmailResult): UserVerifyEmailResponse
}