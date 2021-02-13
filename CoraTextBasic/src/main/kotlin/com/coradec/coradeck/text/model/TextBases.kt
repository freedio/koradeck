package com.coradec.coradeck.text.model

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.core.model.ClassPathResource
import com.coradec.coradeck.text.ctrl.TextbaseReader
import com.coradec.coradeck.text.model.impl.StandardTextBase
import com.coradec.coradeck.text.model.impl.StandardNamedText
import com.coradec.coradeck.text.trouble.ConTextBaseNotFoundException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

object TextBases : Logger() {
    private val allTexts = ConcurrentHashMap<String, Text>()
    private val loadedBases = CopyOnWriteArraySet<String>()

    fun byContext(context: String, locale: Locale): TextBase {
        val contag = "$context.${locale.toLanguageTag()}"
        if (loadedBases.add(contag)) try {
            loadTextBase(context, locale)
        } catch (e: RuntimeException) {
            loadedBases.remove(contag)
            throw e
        }
        return allTexts
            .filter { it.key.startsWith(context) }
            .toTextBase()
    }

    private fun loadTextBase(context: String, locale: Locale) {
        debug("Loading text base «%s»", context)
        val base = context.replace('.', '/')
        val locales = listOf("_${locale.language}_${locale.country}", "_${locale.language}", "")
        for (loc in locales) {
            val resourceName = "${base}$loc.text"
            debug("Trying classpath resource «%s» ...", resourceName)
            if (ClassPathResource(resourceName).ifExists {
                    allTexts.putAll(TextbaseReader.read(location)
                        .map { (key, value) ->
                            val name = "$context.$key"
                            Pair(name, StandardNamedText(name, value.toString()))
                        })
                    loadedBases += context
                }
            ) return
        }
        throw ConTextBaseNotFoundException(context)
    }
}

private fun Map<String, Text>.toTextBase(): TextBase = StandardTextBase(this)
