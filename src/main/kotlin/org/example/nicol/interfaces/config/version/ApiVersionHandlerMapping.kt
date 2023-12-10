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

package org.example.nicol.interfaces.config.version

import org.springframework.core.annotation.AnnotationUtils
import org.springframework.web.servlet.mvc.condition.RequestCondition
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

class ApiVersionHandlerMapping : RequestMappingHandlerMapping() {

    override fun getCustomMethodCondition(method: Method): RequestCondition<*>? {
        return createCondition(method.declaringClass)
    }

    override fun getCustomTypeCondition(handlerType: Class<*>): RequestCondition<*>? {
        return createCondition(handlerType)
    }

    private fun createCondition(clazz: Class<*>): RequestCondition<ApiVersionCondition>? {
        return AnnotationUtils.getAnnotation(clazz, ApiVersion::class.java)
            ?.value?.let { ApiVersionCondition(it) }
    }

    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        val info = super.getMappingForMethod(method, handlerType) ?: return null
        val version = AnnotationUtils.findAnnotation(method, ApiVersion::class.java)?.value
            ?: AnnotationUtils.findAnnotation(handlerType, ApiVersion::class.java)?.value
            ?: return info

        val customCondition = getCustomMethodCondition(method) ?: getCustomTypeCondition(handlerType)

        return customCondition?.let {
            RequestMappingInfo.paths("v$version")
                .build()
                .combine(info.addCustomCondition(it))
        } ?: info
    }

}