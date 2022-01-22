/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.classname
import kotlin.reflect.KClass

class ClassOrigin(val klass: KClass<*>): Origin {
    override fun represent(): String = klass.classname
}
