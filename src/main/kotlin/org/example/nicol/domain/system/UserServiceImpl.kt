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

package org.example.nicol.domain.system

import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.kt.new
import org.bouncycastle.asn1.x500.style.RFC4519Style.name
import org.example.nicol.application.UserService
import org.example.nicol.application.config.NicolProperties
import org.example.nicol.domain.system.exception.BadCredentialsException
import org.example.nicol.domain.system.exception.IncorrectSessionException
import org.example.nicol.domain.system.exception.VerifyEmailExpiredException
import org.example.nicol.domain.system.model.command.CreateUserCommand
import org.example.nicol.domain.system.model.command.LoginUserCommand
import org.example.nicol.domain.system.model.command.RecordVerifyEmailCommand
import org.example.nicol.domain.system.model.command.CheckAccessTokenCommand
import org.example.nicol.domain.system.model.command.GetUserAuthCommand
import org.example.nicol.domain.system.model.command.VerifyEmailCommand
import org.example.nicol.domain.system.model.vo.Payload
import org.example.nicol.domain.system.model.mapper.PayloadMapper
import org.example.nicol.domain.system.model.mapper.UserMapper
import org.example.nicol.domain.system.model.result.LoginUserResult
import org.example.nicol.domain.system.model.result.RenewAccessTokenResult
import org.example.nicol.domain.system.model.result.UserResult
import org.example.nicol.domain.system.model.result.UserVerifyEmailResult
import org.example.nicol.domain.system.model.result.VerifyEmailResult
import org.example.nicol.domain.system.model.vo.UserAuth
import org.example.nicol.infrastructure.repository.UserRepository
import org.example.nicol.domain.system.service.TokenService
import org.example.nicol.infrastructure.computation.HashUtils
import org.example.nicol.infrastructure.computation.randomValue
import org.example.nicol.infrastructure.entity.Session
import org.example.nicol.infrastructure.entity.VerifyEmail
import org.example.nicol.infrastructure.entity.by
import org.example.nicol.infrastructure.repository.SessionRepository
import org.example.nicol.infrastructure.repository.VerifyEmailRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.OffsetDateTime

@Service
class UserServiceImpl(
    val tokenService: TokenService,
    val userRepository: UserRepository,
    val sessionRepository: SessionRepository,
    val verifyEmailRepository: VerifyEmailRepository,
    val userMapper: UserMapper,
    val payloadMapper: PayloadMapper,
    val nicolProperties: NicolProperties,
    val transactionTemplate: TransactionTemplate
) : UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    override fun registerUser(createUserCommand: CreateUserCommand): UserResult {
        val user = userRepository.insert(userMapper.toUser(createUserCommand))
        return userMapper.toUserResult(user)
    }

    override fun loginUser(loginUserCommand: LoginUserCommand): LoginUserResult {
        val user = userRepository.findByUsername(loginUserCommand.username)
            ?: throw BadCredentialsException("User not found with username: ${loginUserCommand.username}")

        HashUtils.checkPassword(loginUserCommand.password, user.hashedPassword)
            .takeIf { it } ?: throw BadCredentialsException("Wrong password")

        val (accessToken, accessPayload) = tokenService.createToken(
            payloadMapper.toPayload(
                loginUserCommand.username,
                nicolProperties.security.accessTokenDuration
            ),
            nicolProperties.security.tokenSymmetricKey,
            nicolProperties.security.footer
        )
        val (refreshToken, refreshPayload) = tokenService.createToken(
            payloadMapper.toPayload(
                loginUserCommand.username,
                nicolProperties.security.refreshTokenDuration
            ),
            nicolProperties.security.tokenSymmetricKey,
            nicolProperties.security.footer
        )
        val session = createSession(user.username, refreshToken, refreshPayload)

        return LoginUserResult(
            sessionId = session.id,
            accessToken = accessToken,
            accessTokenExpiresAt = accessPayload.expiredAt,
            refreshToken = refreshToken,
            refreshTokenExpiresAt = refreshPayload.expiredAt,
            userResult = userMapper.toUserResult(user)
        )
    }

    private fun createSession(
        username: String,
        refreshToken: String,
        refreshPayload: Payload
    ): Session {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request
        val clientIp = getClientIp(request)
        val userAgent = request.getHeader("User-Agent")
        return sessionRepository.insert(new(Session::class).by {
            this.id = refreshPayload.id
            this.username = username
            this.refreshToken = refreshToken
            this.userAgent = userAgent
            this.clientIp = clientIp
            this.expiredAt = refreshPayload.expiredAt
            this.blocked = false
        })
    }

    override fun renewToken(checkTokenCommand: CheckAccessTokenCommand): RenewAccessTokenResult {
        val payload = checkToken(checkTokenCommand)
        checkSession(payload, checkTokenCommand)
        val (token, accessPayload) = tokenService.createToken(
            payloadMapper.toPayload(
                payload.username,
                nicolProperties.security.accessTokenDuration
            ),
            nicolProperties.security.tokenSymmetricKey,
            nicolProperties.security.footer
        )
        return RenewAccessTokenResult(token, accessPayload.expiredAt)
    }

    private fun checkSession(
        payload: Payload,
        checkAccessTokenCommand: CheckAccessTokenCommand
    ) {
        val session = sessionRepository.findById(payload.id)
            .orElseThrow { IncorrectSessionException("User session record not found") }
        when {
            session.blocked -> throw IncorrectSessionException("Blocked session")
            session.username != payload.username ->
                throw IncorrectSessionException("Incorrect session user")

            session.refreshToken != checkAccessTokenCommand.refreshToken ->
                throw IncorrectSessionException("Mismatched session token")

            session.expiredAt.isBefore(OffsetDateTime.now()) ->
                throw IncorrectSessionException("Expired session")
        }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedForHeader = request.getHeader("X-Forwarded-For")
        if (xForwardedForHeader != null && xForwardedForHeader.isNotEmpty()) {
            return xForwardedForHeader.split(",").first()
        }
        return request.remoteAddr
    }

    override fun verifyEmail(verifyEmailCommand: VerifyEmailCommand): UserVerifyEmailResult {
        val userId = verifyEmailRepository.findUserIdByExpiredTimeCheck(
            id = verifyEmailCommand.emailId,
            secretCode = verifyEmailCommand.secretCode
        ) ?: throw VerifyEmailExpiredException("Verify email has been expired or already verified")

        transactionTemplate.execute { status: TransactionStatus ->
            try {
                verifyEmailRepository.updateUsedByExpiredTimeCheck(
                    id = verifyEmailCommand.emailId,
                    secretCode = verifyEmailCommand.secretCode,
                    used = true
                )
                userRepository.updateEmailVerifyByIdAndVerifyCheck(
                    id = userId,
                    emailVerify = true
                )
                status.flush()
            } catch (e: Exception) {
                status.setRollbackOnly()
                logger.error(
                    "userId: {} verify email fail,cause for: {}",
                    userId,
                    e.message
                )
                throw VerifyEmailExpiredException("Verify email has been expired or already verified")
            }
        }
        return UserVerifyEmailResult(emailVerified = true)
    }

    override fun recordVerifyEmail(recordVerifyEmailCommand: RecordVerifyEmailCommand): VerifyEmailResult {
        val verifyEmail = verifyEmailRepository.insert(
            new(VerifyEmail::class).by {
                this.email = recordVerifyEmailCommand.email
                this.secretCode = randomValue<String>(32)
                this.used = false
                this.user().apply {
                    this.id = recordVerifyEmailCommand.userId
                }
            })
        return userMapper.toVerifyEmailResult(verifyEmail)
    }

    override fun checkToken(checkTokenCommand: CheckAccessTokenCommand): Payload =
        tokenService.verifyToken(
            checkTokenCommand.refreshToken,
            nicolProperties.security.tokenSymmetricKey,
            nicolProperties.security.footer
        )

    override fun getUserAuth(getUserAuthCommand: GetUserAuthCommand): UserAuth {
        val user = userRepository.findPasswordByUsername(getUserAuthCommand.username)
            ?: throw BadCredentialsException("User not found with username: ${getUserAuthCommand.username}")
        return userMapper.toUserAuth(user)
    }

}