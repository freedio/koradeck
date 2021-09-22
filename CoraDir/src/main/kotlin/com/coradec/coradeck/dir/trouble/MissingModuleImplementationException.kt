/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.trouble

import kotlin.reflect.KClass

class MissingModuleImplementationException(val type: KClass<*>) : DirectoryException() {

}
