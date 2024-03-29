/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model.impl

import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.model.Views

open class BasicSession(override val user: String, override val createdOn: Thread) : Session {
    private var authPassed = false
    override val authenticated: Boolean get() = user == Session.currentUser || authPassed
    override val view: Views = BasicViews()
}
