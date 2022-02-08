/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl.impl

import com.coradec.coradeck.gui.ctrl.*
import com.coradec.coradeck.gui.model.Container
import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex
import com.coradec.coradeck.gui.model.impl.BasicSection
import com.coradec.coradeck.gui.trouble.LayoutNotInstantiableException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.*

class ApplicationLayout : SectionLayout {
    override val indices: Set<SectionIndex> = setOf(*ApplicationSections.values())
    private val sections = ConcurrentHashMap<SectionIndex, Section>()

    override fun get(index: SectionIndex): Section = sections.computeIfAbsent(index) { BasicSection(index) }

    enum class ApplicationSections(private val defaultLayoutClass: KClass<out Layout>) : SectionIndex {
        MENU_PLANE(MenuLayout::class),
        TITLE_PLANE(TitleLayout::class),
        LEADING_PLANE(VerticalLayout::class),
        CONTENT_PLANE(HorizontalLayout::class),
        TRAILING_PLAIN(VerticalLayout::class),
        STATUS_PLANE(LeadingHorizontalLayout::class),
        CONTROL_PLANE(TrailingHorizontalLayout::class);

        override val defaultLayout: Layout get() =
            if (defaultLayoutClass.isAbstract) {
                defaultLayoutClass.companionObject?.memberFunctions?.singleOrNull { method ->
                    method.name == "invoke" && method.valueParameters.let {
                        it.size == 1 && Container::class.isSuperclassOf(it.iterator().next().type.classifier as KClass<*>)
                    }
                }?.call() as? Layout ?: throw LayoutNotInstantiableException(defaultLayoutClass)
            } else defaultLayoutClass.primaryConstructor?.call()
                ?: throw LayoutNotInstantiableException(defaultLayoutClass)
    }
}
