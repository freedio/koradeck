/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.trouble

import com.coradec.coradeck.core.util.formatted
import java.io.PrintWriter
import java.io.StringWriter

class MultiException(val problems: Collection<Exception>) : BasicException() {
    override val message: String
        get() {
            val properties = listProperties().filterKeys { it != "Problems" }.map { "${it.key}: ${it.value.formatted}"}.joinToString()
            val result = StringWriter()
            PrintWriter(result).use { out ->
                if (superMessage != null) out.print("\"$superMessage\" ")
                if (properties.isNotEmpty()) out.print("($properties)")
                out.println()
                problems.forEach { problem ->
                    problem.printStackTrace(out)
                }
            }
            return result.toString()
        }
}
