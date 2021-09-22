/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.trouble

class NonCompliantTypeConverterException(val converterClass: Class<*>?, problem: Throwable) : TypeException(null, problem) {

}
