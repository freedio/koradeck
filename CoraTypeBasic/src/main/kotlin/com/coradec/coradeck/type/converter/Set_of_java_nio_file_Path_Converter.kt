/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

class Set_of_java_nio_file_Path_Converter: BasicTypeConverter<Set<Path>>(
    Set::class.createType(listOf(KTypeProjection(KVariance.OUT, Path::class.starProjectedType)))
) {
    override fun decodeFrom(value: String): Set<Path>? {
        println("Decoding «%s» to a set of path".format(value))
        TODO("Not yet implemented")
    }

    override fun convertFrom(value: Any): Set<Path>? = when(value) {
        is List<*> -> value.map { Paths.get(it.toString()) }.toSet()
        else -> null
    }
}