/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.trouble

class RequestCancelledException(message: String?, problem: Throwable?) : CommunicationException(message, problem) {
    constructor(problem: Throwable? = null) : this(null, problem)
}
