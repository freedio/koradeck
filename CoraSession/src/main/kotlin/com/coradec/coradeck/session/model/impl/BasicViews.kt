/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model.impl

import com.coradec.coradeck.session.model.Views
import com.coradec.coradeck.session.view.View
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class BasicViews : Views {
    private val views = ConcurrentHashMap<Any, ConcurrentHashMap<KClass<*>, View>>()

    override fun <ViewType : View> get(owner: Any, type: KClass<ViewType>): ViewType? =
        views[owner]?.get(type) as? ViewType
    override fun <ViewType : View> set(owner: Any, type: KClass<ViewType>, view: ViewType) {
        views.computeIfAbsent(owner) { ConcurrentHashMap() }[type] = view
    }
}
