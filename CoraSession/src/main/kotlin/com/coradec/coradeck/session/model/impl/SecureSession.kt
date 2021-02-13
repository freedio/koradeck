package com.coradec.coradeck.session.model.impl

import com.coradec.coradeck.session.model.Session

class SecureSession(override val user: String) : Session {
    private var authPassed = false
    override val authenticated: Boolean get() = authPassed
}
