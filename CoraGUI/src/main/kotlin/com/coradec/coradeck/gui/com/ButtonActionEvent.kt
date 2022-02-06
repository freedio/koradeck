/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.com

import com.coradec.coradeck.com.model.impl.BasicEvent
import com.coradec.coradeck.core.model.Origin

class ButtonActionEvent(origin: Origin, val modifiers: Int, val command: String?): BasicEvent(origin)
