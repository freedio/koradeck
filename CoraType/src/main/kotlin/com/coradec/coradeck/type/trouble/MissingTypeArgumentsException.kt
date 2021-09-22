/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.trouble

class MissingTypeArgumentsException(val parameters: Collection<String>) : TypeException("Missing type arguments for parameters!") {

}
