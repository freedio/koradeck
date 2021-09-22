/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

import com.coradec.coradeck.dir.module.CoraDir
import kotlin.reflect.KType

interface TypeFolder<T: Any>: TypeEntry<T> {
    /** Returns the collection of instances. */
    val instances: Collection<T>

    /** Adds the specified object as an instance of the folder's type. */
    fun addInstance(obj: T)

    companion object {
        operator fun <T: Any> invoke(type: KType) = CoraDir.createTypeFolder<T>(type)
    }
}
