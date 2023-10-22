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

package org.example.nicol.interfaces.restful

import org.example.nicol.domain.system.model.result.LoginUserResult
import org.example.nicol.domain.system.model.result.RenewAccessTokenResult
import org.example.nicol.domain.system.model.result.UserResult
import org.example.nicol.application.UserService
import org.example.nicol.domain.system.model.command.VerifyEmailCommand
import org.example.nicol.interfaces.payload.mapper.UserDtoMapper
import org.example.nicol.interfaces.payload.request.CreateUserRequest
import org.example.nicol.interfaces.payload.request.LoginUserRequest
import org.example.nicol.interfaces.payload.request.RenewAccessTokenRequest
import org.example.nicol.interfaces.payload.response.LoginUserResponse
import org.example.nicol.interfaces.payload.response.RenewAccessTokenResponse
import org.example.nicol.interfaces.payload.response.UserResponse
import org.example.nicol.interfaces.config.version.ApiVersion
import org.example.nicol.interfaces.payload.response.UserVerifyEmailResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@ApiVersion
@RestController
@RequestMapping("/user")
class UserController(
    val userService: UserService,
    val userDtoMapper: UserDtoMapper,
) {
    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody createUserRequest: CreateUserRequest): ResponseEntity<UserResponse> {
        val user: UserResult = userService.registerUser(
            userDtoMapper.toCreateUserCommand(createUserRequest)
        )
        return ResponseEntity(userDtoMapper.toUserResponse(user), HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun loginUser(@Valid @RequestBody loginUserRequest: LoginUserRequest): ResponseEntity<LoginUserResponse> {
        val loginUser: LoginUserResult = userService.loginUser(
            userDtoMapper.toLoginUserCommand(loginUserRequest)
        )
        return ResponseEntity.ok(userDtoMapper.toLoginUserResponse(loginUser))
    }

    @PostMapping("/renew-access")
    fun renewUser(@Valid @RequestBody renewAccessTokenRequest: RenewAccessTokenRequest):
            ResponseEntity<RenewAccessTokenResponse> {
        val renewAccessTokenResult: RenewAccessTokenResult =
            userService.renewToken(userDtoMapper.toRenewAccessTokenCommand(renewAccessTokenRequest))
        return ResponseEntity.ok(userDtoMapper.toRenewAccessTokenResponse(renewAccessTokenResult))
    }

    @GetMapping("/verify-email/{id}/{secretCode}")
    fun verifyEmail(
        @PathVariable
        @NotNull(message = "id must be not null")
        @Min(value = 1, message = "id must be greater than or equal to 1")
        id: Long,
        @PathVariable
        @NotBlank(message = "secretCode must be not null")
        secretCode: String
    ): ResponseEntity<UserVerifyEmailResponse> {
        val verifyEmailResult = userService.verifyEmail(VerifyEmailCommand(id, secretCode))
        return ResponseEntity.ok(userDtoMapper.toUserVerifyEmailResponse(verifyEmailResult))
    }
}