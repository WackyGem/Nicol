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
import org.example.nicol.application.UserService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@ConditionalOnWebApplication
class WebSecurityConfig(
    val userService: UserService,
    val objectMapper: ObjectMapper
) {

    companion object {
        val SWAGGER_STATIC_RESOURCES_PATH = arrayOf(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
        )
        val POST_AUTH_PASS_PATH = arrayOf(
            "/*/user/register",
            "/*/user/login",
            "/*/user/renew-access",
            "/*/chat/completions"
        )
        val GET_AUTH_PASS_PATH = arrayOf(
            "/*/user/verify_email",
        )
    }

    @ConditionalOnProperty(name = ["nicol.security.enabled"], havingValue = "true", matchIfMissing = true)
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain = httpSecurity
        .httpBasic { it.disable() }
        .csrf { it.disable() }
        .formLogin { it.disable() }
        .logout { it.disable() }
        .exceptionHandling {
            it.authenticationEntryPoint(UnauthorizedEntryPoint(objectMapper))
                .accessDeniedHandler(DefaultAccessDeniedHandler(objectMapper))
        }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.requestMatchers(HttpMethod.POST, *POST_AUTH_PASS_PATH)
                .permitAll()
                .requestMatchers(HttpMethod.GET, *GET_AUTH_PASS_PATH)
                .permitAll()
                .requestMatchers(*SWAGGER_STATIC_RESOURCES_PATH)
                .permitAll()
                .anyRequest()
                .authenticated()
        }
        .addFilterBefore(
            TokenAuthenticationFilter(
                userService,
                objectMapper,
                (SWAGGER_STATIC_RESOURCES_PATH
                    .plus(POST_AUTH_PASS_PATH)
                    .plus(GET_AUTH_PASS_PATH))
            ),
            UsernamePasswordAuthenticationFilter::class.java
        )
        .build()

    @ConditionalOnProperty(name = ["nicol.security.enabled"], havingValue = "false")
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun nonAuthorizeFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain = httpSecurity
        .httpBasic { it.disable() }
        .csrf { it.disable() }
        .formLogin { it.disable() }
        .logout { it.disable() }
        .exceptionHandling {
            it.authenticationEntryPoint(UnauthorizedEntryPoint(objectMapper))
                .accessDeniedHandler(DefaultAccessDeniedHandler(objectMapper))
        }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.anyRequest()
                .permitAll()
        }
        .build()

}