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

package org.example.nicol.infrastructure.enhance.strategy

import org.apache.commons.lang3.ClassUtils
import org.springframework.aop.TargetClassAware
import org.springframework.aop.framework.Advised
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.util.concurrent.ConcurrentHashMap

@Component
class StrategyMapPostProcessor : BeanPostProcessor {

    private data class StrategyMapIdentifier(val keyClazz: Class<*>, val valueClazz: Class<*>)

    private val strategyMaps: MutableMap<StrategyMapIdentifier, MutableMap<Any, MutableList<Any>>> =
        ConcurrentHashMap()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        for (method in getMethods(bean.javaClass)) {
            AnnotationUtils.findAnnotation(method, StrategyKey::class.java)?.let {
                val key = method.invoke(bean)
                val keyClass = key::class.java
                val valueClass = getMethodDeclarer(method)
                val map = strategyMaps.getOrPut(StrategyMapIdentifier(keyClass, valueClass)) { mutableMapOf() }
                val list = map.computeIfAbsent(key) { mutableListOf() }
                list += bean
            }
        }
        fillStrategyMap(bean)
        return bean
    }

    private fun fillStrategyMap(bean: Any) {
        var handler: Any? = bean
        if (handler is TargetClassAware) {
            handler = (handler as Advised).targetSource.target
        }
        handler ?: error("Handler is null")
        val fields = getFields(handler.javaClass)

        for (field in fields) {
            if (field.isAnnotationPresent(StrategyMap::class.java)) {
                if (!Map::class.java.isAssignableFrom(field.type)) {
                    error("@StrategyMap may only be used on maps")
                }
                val keyClazz = getGenericClass(field, 0)!!
                val valueClazz = getGenericClass(field, 1)!!
                val isList = Collection::class.java.isAssignableFrom(valueClazz)
                ReflectionUtils.makeAccessible(field)
                val map = strategyMaps.getOrPut(
                    StrategyMapIdentifier(
                        keyClazz, if (isList) resolveNestedGeneric(field, 1)
                        else valueClazz
                    )
                ) { mutableMapOf() }
                field[handler] =
                    if (isList) map
                    else SingleValueMap(map)
            }
        }
    }

    fun getMethodDeclarer(method: Method): Class<*> {
        var declaringClass: Class<*> = method.declaringClass
        val methodName: String = method.name
        val parameterTypes: Array<Class<*>> = method.parameterTypes
        for (interfaceType in ClassUtils.getAllInterfaces(declaringClass))
            return interfaceType.getMethod(methodName, *parameterTypes).declaringClass
        while (true) {
            declaringClass = declaringClass.superclass ?: break
            try {
                val newMethod = declaringClass.getMethod(methodName, *parameterTypes)
                return getMethodDeclarer(newMethod)
            } catch (ex: NoSuchMethodException) {
                break
            }
        }
        error("Could not find method declarer for ${method.declaringClass.canonicalName}::${method.name}")
    }

    fun getGenericClass(field: Field, index: Int): Class<*>? {
        val genericType = field.genericType as? ParameterizedType ?: return null
        val typeArgument = genericType.actualTypeArguments[index]

        if (typeArgument is ParameterizedType) {
            return typeArgument.rawType as Class<*>
        }

        return (typeArgument as? Class<*>) ?: (typeArgument as? WildcardType)?.upperBounds?.get(0) as? Class<*>
    }

    fun resolveNestedGeneric(field: Field, parentIndex: Int, childIndex: Int = 0): Class<*> {
        val genericType = field.genericType as? ParameterizedType
            ?: error("${field.type} is not a parameterized type")
        var childType = genericType.actualTypeArguments[parentIndex]

        while (childType is WildcardType) {
            childType = childType.upperBounds[0]
        }

        return when (childType) {
            is ParameterizedType -> {
                var returnValue = childType.actualTypeArguments[childIndex]
                while (returnValue is WildcardType) {
                    returnValue = returnValue.upperBounds[0]
                }
                returnValue as Class<*>
            }

            else -> childType as Class<*>
        }
    }

    fun getFields(type: Class<*>): List<Field> =
        generateSequence(type) { it.superclass }
            .flatMap { it.declaredFields.asSequence() }
            .toList()

    fun getMethods(type: Class<*>): List<Method> =
        generateSequence(type) { it.superclass }
            .flatMap { it.declaredMethods.asSequence() }
            .toList()

    class SingleValueMap<K, V>(private val originalMap: Map<K, Collection<V>>) : Map<K, V> {
        override val size: Int
            get() = originalMap.size

        override val values: Collection<V>
            get() = originalMap.values.map { it.first() }

        override val entries: Set<Map.Entry<K, V>>
            get() = originalMap.map {
                it.key to it.value.first()
            }.toMap().entries

        override val keys: Set<K>
            get() = originalMap.keys

        override fun containsKey(key: K): Boolean {
            return originalMap.containsKey(key)
        }

        override fun containsValue(value: V): Boolean {
            return originalMap.values.any { it.first() == value }
        }

        override fun get(key: K): V? {
            return originalMap[key]?.first()
        }

        override fun isEmpty(): Boolean {
            return originalMap.isEmpty()
        }
    }
}

