/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.trouble

class ClassPathResourceNotFoundException(val path: String, val workDir: String = System.getProperty("user.dir")) :
        BasicException("Resource not found on the class path!")
