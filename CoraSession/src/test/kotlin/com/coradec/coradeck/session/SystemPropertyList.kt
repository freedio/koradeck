/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session

import org.junit.jupiter.api.Test

class SystemPropertyList {

    @Suppress("UNCHECKED_CAST")
    @Test fun showSystemProperties() {
        println("System Properties")
        println("-----------------")
        val properties = System.getProperties() as Map<String, String>
        properties.toSortedMap().forEach { (name, property) ->
            println("$name: \"${property.toString()
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
            }\"")
        }
    }

}
