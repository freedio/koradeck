/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.trouble

import com.coradec.coradeck.core.trouble.BasicException

open class BasicGUIException(message: String? = null, problem: Throwable? = null) : BasicException(message, problem)
