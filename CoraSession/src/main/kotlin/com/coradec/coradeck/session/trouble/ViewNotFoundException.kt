/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.trouble

import kotlin.reflect.KClass

class ViewNotFoundException(val ownerType: KClass<*>, val viewType: KClass<*>): SessionException()
