package com.cbruegg.mensaupb.parser

import com.squareup.moshi.ClassFactory
import java.lang.reflect.Constructor
import java.lang.reflect.Proxy

/**
 * A [ClassFactory] that supplies a constructor annotated with
 * a single [JsonConstructor] with non-null bogus arguments. This is useful
 * to also initialize the delegates of delegated properties
 * when deserializing JSON data into Kotlin classes.
 */
class KotlinDataClassFactory<T : Any>(rawType: Class<T>) : ClassFactory<T>() {

    private val constructor: Constructor<T>
    private val constructorArgs: Array<Any>

    init {
        val annotatedConstructors = rawType.declaredConstructors.filter { it.isAnnotationPresent(JsonConstructor::class.java) }
        if (annotatedConstructors.size != 1) {
            throw IllegalArgumentException("Class $rawType should not have more than one " +
                    "or no constructor annotated with @JsonConstructor.")
        }
        @Suppress("UNCHECKED_CAST")
        (constructor) = annotatedConstructors.first() as Constructor<T>
        constructor.isAccessible = true

        constructorArgs = inventArgsFor(constructor)
    }

    override fun newInstance(): T = constructor.newInstance(*constructorArgs)

}

/**
 * Create an array of arguments for the constructor that will
 * most likely make the constructor call succeed.
 *
 * - Primitive types and their associated reference types get the default value, such as 0 for int and Integer, 0.0 for double and Double and so on.
 * - Strings get "".
 * - Interfaces get a proxy instance that simply throws on method calls.
 * - Enums get the first value in the values field.
 * - Other types are recursively constructed.
 */
private fun <T> inventArgsFor(constructor: Constructor<T>): Array<Any> {
    val parameterTypes = constructor.parameterTypes
    return Array(parameterTypes.size) { i ->
        val parameterType = parameterTypes[i]
        when {
            parameterType == Byte::class.javaPrimitiveType || parameterType == Byte::class.java -> 0.toByte()
            parameterType == Short::class.javaPrimitiveType || parameterType == Short::class.java -> 0.toShort()
            parameterType == Int::class.javaPrimitiveType || parameterType == Int::class.java -> 0
            parameterType == Long::class.javaPrimitiveType || parameterType == Long::class.java -> 0L
            parameterType == Float::class.javaPrimitiveType || parameterType == Float::class.java -> 0f
            parameterType == Double::class.javaPrimitiveType || parameterType == Double::class.java -> 0.0
            parameterType == Char::class.javaPrimitiveType || parameterType == Char::class.java -> '\u0000'
            parameterType == Boolean::class.javaPrimitiveType || parameterType == Boolean::class.java -> false
            parameterType == String::class.java -> ""
            parameterType.isInterface -> Proxy.newProxyInstance(constructor.javaClass.classLoader, arrayOf(parameterType)) { _, _, _ ->
                throw UnsupportedOperationException("Trying to call a method on a " +
                        "stub object created by Moshi as requested using the JsonConstructor" +
                        "annotation.")
            }
            parameterType.isEnum && parameterType.enumConstants.isNotEmpty() -> parameterType.enumConstants[0]
            else -> try {
                KotlinDataClassFactory(parameterType).newInstance()
            } catch (_: Exception) {
                ClassFactory.get<Any>(parameterType).newInstance()
            }
        }
    }
}

/**
 * @see [KotlinDataClassFactory]
 */
annotation class JsonConstructor

/**
 * @see [KotlinDataClassFactory]
 */
inline fun <reified T : Any> KotlinDataClassFactory() = KotlinDataClassFactory(T::class.java)