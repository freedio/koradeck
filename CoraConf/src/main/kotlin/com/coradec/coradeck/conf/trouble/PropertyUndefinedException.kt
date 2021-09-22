/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.trouble

import kotlin.reflect.KType

class PropertyUndefinedException(val name: String, val type: KType) : ConfigurationException()
