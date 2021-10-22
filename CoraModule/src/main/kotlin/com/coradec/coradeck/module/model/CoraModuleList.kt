/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.module.model

import com.coradec.coradeck.module.trouble.MissingModuleImplementationException
import kotlin.reflect.KClass

class CoraModuleList<M: CoraModuleAPI>(val klass: KClass<out CoraModule<M>>, val modules: List<M>) {
    val best: M get() = modules.lastOrNull() ?: throw MissingModuleImplementationException(klass)
}
