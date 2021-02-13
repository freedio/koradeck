package com.coradec.conf.model

import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.ctrl.impl.JsonConfigurationReaderUT
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class LocalPropertyUT {
    val p1 = LocalProperty("P1", 10)
    val p2 = LocalProperty("P2", "Hello, World!")

    @Test fun propertyValue() {
        // given:
        // when:
        val r1: Int = p1.value
        val r2: String = p2.value
        // then:
        assertThat(r1).isEqualTo(12)
        assertThat(r2).isEqualTo("Hello, World!")
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraComImpl(), CoraTextImpl(), CoraConfImpl(), CoraTypeImpl())
        }

        @AfterAll
        @JvmStatic fun teardown() {
            CoraModules.initialize()
        }

    }
}