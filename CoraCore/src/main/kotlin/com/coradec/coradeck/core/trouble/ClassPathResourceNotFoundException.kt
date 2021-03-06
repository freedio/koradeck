package com.coradec.coradeck.core.trouble

import com.coradec.coradeck.core.trouble.BasicException

class ClassPathResourceNotFoundException(val path: String, val workDir: String = System.getProperty("user.dir")) :
        BasicException("Resource not found on the class path!")
