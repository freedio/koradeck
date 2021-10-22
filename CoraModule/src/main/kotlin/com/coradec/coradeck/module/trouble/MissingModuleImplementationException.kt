/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.module.trouble

import kotlin.reflect.KClass

class MissingModuleImplementationException(val type: KClass<*>) : ModuleException() {

}
