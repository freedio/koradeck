/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.module.trouble

import com.coradec.coradeck.module.model.CoraModuleAPI
import kotlin.reflect.KClass

class ModuleWithoutPrimaryConstructorException(klass: KClass<out CoraModuleAPI>) : ModuleException()
