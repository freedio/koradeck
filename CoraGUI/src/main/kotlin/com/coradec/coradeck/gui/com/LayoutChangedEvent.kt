/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.com

import com.coradec.coradeck.com.model.impl.BasicEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.gui.ctrl.Layout

class LayoutChangedEvent(origin: Origin, val layout: Layout): BasicEvent(origin)
