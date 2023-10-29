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

package org.example.nicol.domain.system.model.mapper

import org.example.nicol.domain.system.model.command.CreateUserCommand
import org.example.nicol.domain.system.model.command.RecordVerifyEmailCommand
import org.example.nicol.domain.system.model.result.UserResult
import org.example.nicol.domain.system.model.result.VerifyEmailResult
import org.example.nicol.domain.system.model.vo.UserAuth
import org.example.nicol.infrastructure.computation.HashUtils
import org.example.nicol.infrastructure.entity.User
import org.example.nicol.infrastructure.entity.VerifyEmail
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import java.util.Collections

@Mapper(componentModel = "spring",imports = [HashUtils::class, Collections::class])
interface UserMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target="hashedPassword", expression="java(HashUtils.hashPassword(createUserCommand.getPassword()))")
    fun toUser(createUserCommand:CreateUserCommand): User

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toUserResult(user: User) : UserResult

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toRecordVerifyEmailCommand(userResult: UserResult): RecordVerifyEmailCommand

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toVerifyEmailResult(verifyEmail: VerifyEmail):VerifyEmailResult

    @Mappings(
        Mapping(source = "username", target  = "username"),
        Mapping(source = "hashedPassword", target = "password"),
        Mapping(target = "authorities", expression = "java(Collections.emptyList())")
    )
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toUserAuth(user: User):UserAuth
}