/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.core.model.ClassPathResource
import com.coradec.coradeck.text.ctrl.TextbaseReader
import com.coradec.coradeck.text.model.impl.GeneralTextBase
import com.coradec.coradeck.text.model.impl.TextElement
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

object TextBases : Logger() {
    private val allTexts = ConcurrentHashMap<String, TextElement>()
    private val loadedBases = CopyOnWriteArraySet<String>()
    private val bases = ConcurrentHashMap<String, Map<String, String>>()

    fun byContext(context: String): TextBase {
        if (loadedBases.add(context)) loadTextBase(context)
        return allTexts
            .filter { it.key.startsWith(context) }
            .mapKeys { (key, _) -> key.substring(context.length + 1) }
            .toTextBase()
    }

    private fun loadTextBase(context: String) {
        val base = context.replace('.', '/')
        val resourceName = "$base.text"
        ClassPathResource(resourceName).ifExists {
            allTexts.putAll(TextbaseReader.read(location).map { (key, value) ->
                val name = "$context.$key"
                Pair(name, TextElement(base, context, name, value)) })
        }
    }

    fun loadLocalizedTextBase(context: String, locale: Locale) = "$context:${locale.toLanguageTag()}".let { basekey ->
        bases.computeIfAbsent(basekey) {
            val base = context.replace('.', '/')
            val resourceNameTemplate = "$base%s.text"
            val locales = listOf(
                "_${locale.language}_${locale.script}_${locale.country}_${locale.variant}",
                "_${locale.language}_${locale.script}_${locale.country}",
                "_${locale.language}_${locale.script}",
                "_${locale.language}_${locale.country}_${locale.variant}",
                "_${locale.language}_${locale.country}",
                "_${locale.language}",
                ""
            )
            val tbase = HashMap<String, String>()
            for (loc in locales) {
                val resourceName = resourceNameTemplate.format(loc)
                if (ClassPathResource(resourceName).ifExists {
                        tbase.putAll(TextbaseReader.read(location).map { (key, value) ->
                            val name = "$context.$key"
                            Pair(name, value)
                        }.toMap())
                    }) break
            }
            tbase
        }
    }
}

private fun Map<String, TextElement>.toTextBase(): TextBase = GeneralTextBase(this)
