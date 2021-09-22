/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.trouble

class TypeConverterNotFoundException(val type: String? = null, val converterClass: String) : TypeException()
