package com.coradec.coradeck.ctrl.impl

import org.junit.jupiter.api.Test

class ShowProperties {
    @Test fun showProperties() {
        System.getProperties().entries.sortedBy { it.key as String }.forEach { (name, value) -> println("$name -> \"$value\"") }
    }
}