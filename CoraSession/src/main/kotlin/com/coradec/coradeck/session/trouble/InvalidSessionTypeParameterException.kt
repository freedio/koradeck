/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.trouble

import com.coradec.coradeck.session.model.SessionType
import com.coradec.coradeck.text.model.LocalText

class InvalidSessionTypeParameterException(parameter: String) :
        SessionException(TEXT_SESSION_TYPE_PROBLEM[parameter, SessionType.values()]) {
    companion object {
        val TEXT_SESSION_TYPE_PROBLEM = LocalText("SessionTypeProblem")
    }
}
