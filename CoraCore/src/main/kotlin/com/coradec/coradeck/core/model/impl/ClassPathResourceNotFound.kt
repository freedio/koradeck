package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.trouble.BasicException

class ClassPathResourceNotFound(val path: String) : BasicException("Resource not found on the class path!") {

}
