/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.trouble

import com.coradec.coradeck.core.model.Timespan

class StandbyTimeoutException(val delay: Timespan) : BasicException()
