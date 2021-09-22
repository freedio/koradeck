/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import kotlin.reflect.KType

abstract class NamedProperty<P: Any>(val name: String, type: KType): BasicProperty<P>(type)
