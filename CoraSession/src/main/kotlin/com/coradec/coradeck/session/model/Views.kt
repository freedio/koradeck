/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model

import com.coradec.coradeck.session.view.View
import kotlin.reflect.KClass

interface Views {
    operator fun <ViewType: View> get(owner: Any, type: KClass<ViewType>): ViewType?
    operator fun <ViewType: View> set(owner: Any, type: KClass<ViewType>, value: ViewType)
}
