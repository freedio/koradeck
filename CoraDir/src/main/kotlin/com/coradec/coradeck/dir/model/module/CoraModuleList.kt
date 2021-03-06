package com.coradec.coradeck.dir.model.module

import com.coradec.coradeck.dir.trouble.MissingModuleImplementationException
import kotlin.reflect.KClass

class CoraModuleList<M: CoraModuleAPI>(val klass: KClass<out CoraModule<M>>, val modules: List<M>) {
    val best: M get() = modules.lastOrNull() ?: throw MissingModuleImplementationException(klass)
}
