package com.coradec.coradeck.type.trouble

class NonCompliantTypeConverterException(val converterClass: Class<*>?, problem: Throwable) : TypeException(null, problem) {

}
