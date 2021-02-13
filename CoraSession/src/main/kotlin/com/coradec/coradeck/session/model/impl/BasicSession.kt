package com.coradec.coradeck.session.model.impl

import com.coradec.coradeck.session.model.Session

class BasicSession(override val user: String) : Session {
    private var authPassed = false
    override val authenticated: Boolean get() = user == Session.currentUser || authPassed
}
