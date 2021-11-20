/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.session.model.Session

abstract class AbstractMemberView(override val session: Session) : MemberView
