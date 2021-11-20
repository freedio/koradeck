/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model.impl

class SecureSession(user: String, createdOn: Thread) : BasicSession(user, createdOn) {
    private var authPassed = false
    override val authenticated: Boolean get() = authPassed
}
