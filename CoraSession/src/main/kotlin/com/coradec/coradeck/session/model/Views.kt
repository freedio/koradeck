/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model

import com.coradec.coradeck.session.view.View
import kotlin.reflect.KClass

interface Views {
    /** Looks up the view of the specified type of the specified owner. */
    operator fun <ViewType : View> get(owner: Any, type: KClass<ViewType>): ViewType?
    /** Sets the view of the specified type in the specified owner. */
    operator fun <ViewType : View> set(owner: Any, type: KClass<ViewType>, view: ViewType)
}
