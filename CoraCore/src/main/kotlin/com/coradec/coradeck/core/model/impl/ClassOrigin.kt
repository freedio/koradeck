package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.classname
import kotlin.reflect.KClass

class ClassOrigin(val klass: KClass<*>): Origin {
    override val representation: String get() = klass.classname
}