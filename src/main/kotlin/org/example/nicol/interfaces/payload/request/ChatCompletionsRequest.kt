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

package org.example.nicol.interfaces.payload.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.Valid
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.example.nicol.domain.inference.enums.MessageRole
import java.io.IOException
import kotlin.reflect.KClass

data class ChatCompletionsRequest(
    @field:NotBlank(message = "model must be not null")
    val model: String,

    @field:Valid
    @field:NotEmpty
    val messages: List<Message>,

    @field:Min(0)
    @field:Max(1)
    val temperature: Float? = 1.0f,

    @field:Min(0)
    @field:Max(1)
    @JsonProperty("top_p")
    val topP: Float? = 1.0f,

    @field:Min(1)
    @field:Max(1)
    val n: Int? = 1,

    @field:Min(1)
    @field:Max(256)
    @JsonProperty("max_tokens")
    val maxTokens: Int? = 256,

    val stream: Boolean = true
) {
    data class Message(
        @field:NotNull
        @field:ValidRole
        @JsonDeserialize(using = UppercaseDeserializer::class)
        val role: String,

        @field:NotBlank
        val content: String
    )

}

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ValidRoleValidator::class])
annotation class ValidRole(
    val message: String = "Invalid role",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)

class ValidRoleValidator : ConstraintValidator<ValidRole, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean =
        value?.lowercase() in MessageRole.values().map { it.name.lowercase() }
}

class UppercaseDeserializer : JsonDeserializer<String>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String =
        p.valueAsString.uppercase()
}
