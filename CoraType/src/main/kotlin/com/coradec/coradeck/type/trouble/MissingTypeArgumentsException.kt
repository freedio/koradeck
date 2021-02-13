package com.coradec.coradeck.type.trouble

class MissingTypeArgumentsException(val parameters: Collection<String>) : TypeException("Missing type arguments for parameters!") {

}
