/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.TypeFolder
import kotlin.reflect.KType

open class BasicTypeFolder<T: Any>(parent: Directory?, name: String, val type: KType):
        BasicDirectoryEntry(parent, name), TypeFolder<T> {
    private val instanceList = ArrayList<T>()
    override val instances: Collection<T> = instanceList

    override fun addInstance(obj: T) {
        synchronized(instanceList) { instanceList += obj }
    }
}
