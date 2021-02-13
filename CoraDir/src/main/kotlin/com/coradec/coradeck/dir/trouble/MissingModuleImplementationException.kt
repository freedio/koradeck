package com.coradec.coradeck.dir.trouble

import com.coradec.coradeck.dir.model.module.CoraModule
import kotlin.reflect.KClass

class MissingModuleImplementationException(val type: KClass<*>) : DirectoryException() {

}
