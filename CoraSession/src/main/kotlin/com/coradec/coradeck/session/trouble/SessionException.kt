/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.trouble

import com.coradec.coradeck.core.trouble.BasicException

open class SessionException(message: String? = null, problem: Throwable? = null) : BasicException(message, problem) {
    constructor(message: String) : this(message, null)
}
