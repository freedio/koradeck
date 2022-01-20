/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.conf.model

import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.conf.trouble.PropertyUndefinedException
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class LocalPropertyUT {
    private val p1 = LocalProperty("P1", 10)
    private val p2 = LocalProperty("P2", "Hello, World!")
    private val p3 = LocalProperty<String>("P3")

    @Test fun propertyValue() {
        // given:
        // when:
        val r1: Int = p1.value
        val r2: String = p2.value
        val r3 = try {
            p3.value
        } catch (e: PropertyUndefinedException) {
            e
        }
        // then:
        assertThat(r1).isEqualTo(12)
        assertThat(r2).isEqualTo("Hello, World!")
        assertThat(r3).isInstanceOf(PropertyUndefinedException::class.java)
            .withFailMessage("(Name: \"com.coradec.conf.model.LocalPropertyUT.P3\", Type: kotlin.String?)")
    }

    @Test fun overriddenPropertyValue() {
        // given:
        System.setProperty(LocalPropertyUT.classname + ".P1", "25")
        System.setProperty(LocalPropertyUT.classname + ".P3", "25")
        // when:
        val r1: Int = p1.value
        val r2: String = p2.value
        val r3 = try {
            p3.value
        } catch (e: PropertyUndefinedException) {
            e
        }
        // then:
        assertThat(r1).isEqualTo(25)
        assertThat(r2).isEqualTo("Hello, World!")
        assertThat(r3).isEqualTo("25")
        // cleanup:
        System.clearProperty(LocalPropertyUT.classname + ".P1")
        System.clearProperty(LocalPropertyUT.classname + ".P3")
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraComImpl::class, CoraTextImpl::class, CoraConfImpl::class, CoraTypeImpl::class)
        }

        @AfterAll
        @JvmStatic fun teardown() {
            CoraModules.initialize()
        }

    }
}
