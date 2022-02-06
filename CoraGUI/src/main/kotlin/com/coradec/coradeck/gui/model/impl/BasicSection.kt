/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.gui.model.Container
import com.coradec.coradeck.gui.model.Section
import java.util.concurrent.ConcurrentHashMap

class BasicSection(private val container: Container) : Section {
    private val components = ConcurrentHashMap<String, MemberView>()

    override fun contains(name: String) = container.contains(name)
    override fun add(name: String, component: MemberView): Request = container.add(name, component) andThen {
        components[name] = component
    }
}
