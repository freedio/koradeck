/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.module.db.trouble

import com.coradec.coradeck.core.trouble.BasicException

class ExcessResultsException(message: String?, problem: Throwable?) : BasicException(message, problem) {
    constructor(message: String) : this(message, null)
}
