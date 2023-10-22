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

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.nicol.application.UserService
import org.example.nicol.domain.system.exception.TokenExpiredException
import org.example.nicol.domain.system.exception.UnauthorizedException
import org.example.nicol.domain.system.model.command.CheckAccessTokenCommand
import org.example.nicol.domain.system.model.command.GetUserAuthCommand
import org.example.nicol.interfaces.enums.ErrorCode
import org.example.nicol.interfaces.payload.response.ErrorResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

class TokenAuthenticationFilter(
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val noFilterList: Array<String>?
) : OncePerRequestFilter() {
    companion object {
        const val AUTH_HEADER_KEY = "authorization"
        const val AUTH_TYPE = "bearer"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        checkHeaderToken(request).onFailure { ex ->
            val errorResponse = when (ex) {
                is UnauthorizedException, is TokenExpiredException ->
                    Pair(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorResponse(ErrorCode.UNAUTHORIZED, ex.message)
                    )

                else ->
                    Pair(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse(ErrorCode.UNKNOWABLE, ex.message)
                    )
            }
            response.status = errorResponse.first
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            objectMapper.writeValue(response.outputStream, errorResponse.second)
        }.onSuccess {
            val userAuth = userService.getUserAuth(GetUserAuthCommand(it.username))
            val emptyContext = SecurityContextHolder.createEmptyContext()
            emptyContext.authentication = UsernamePasswordAuthenticationToken(userAuth, null)
            SecurityContextHolder.setContext(emptyContext)
            filterChain.doFilter(request, response)
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return noFilterList?.any { AntPathMatcher().match(it, request.servletPath) } ?: false
    }

    private fun checkHeaderToken(request: HttpServletRequest) = runCatching {
        val authorizationHeader = request.getHeader(AUTH_HEADER_KEY)
            ?.takeIf { it.isNotBlank() }
            ?: throw UnauthorizedException("Authorization header is missing or empty.")

        val fields = authorizationHeader.split("\\s+".toRegex())
            .takeIf { it.size == 2 }
            ?: throw UnauthorizedException("Invalid authorization header format.")

        fields[0].takeUnless { it == AUTH_TYPE }
            ?: throw UnauthorizedException("Unsupported authorization type ${fields[0]}")

        userService.checkToken(CheckAccessTokenCommand(fields[1]))
    }


}