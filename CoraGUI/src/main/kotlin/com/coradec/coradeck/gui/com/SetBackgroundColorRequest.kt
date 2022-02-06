/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.com

import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.core.model.Origin
import java.awt.Color

class SetBackgroundColorRequest(origin: Origin, val color: Color?): BasicRequest(origin)
