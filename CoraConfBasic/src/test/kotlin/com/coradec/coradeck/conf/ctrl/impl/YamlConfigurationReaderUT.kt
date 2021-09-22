/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConf
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.resource
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger

internal class YamlConfigurationReaderUT {
    val myConfig = resource(this@YamlConfigurationReaderUT::class, ".yaml").location

    companion object {
        @BeforeAll @JvmStatic fun setup() {
            CoraModules.register(CoraComImpl(), CoraTextImpl(), CoraConfImpl())
            writeSaneConfig()
        }

        private fun writeSaneConfig() {
            val nodeFactory = JsonNodeFactory.instance
            val config = ObjectNode(nodeFactory)
            config.put("TextProperty", "String Value")
            config.put("BooleanValue", true)
            config.set<NullNode>("NullProperty", null)
            config.put("ShortProperty", 2.toShort())
            config.put("IntProperty", -100000)
            config.put("LongProperty", 1234567890123456789L)
            config.put("FloatProperty", 12.5f)
            config.put("DoubleProperty", 1.2E+100)
            config.put("DecimalProperty",
                    BigDecimal("1234567890.12345678901234567890123456789012345678901234567890")
            )
            config.put("BigIntProperty",
                    BigInteger("34587326450827059813745091837540193847102934861085610834761092384710875610823471092384710965")
            )
            config.put("BinaryProperty", ByteArray(256) { it.toByte() })
            CoraConf.yamlMapper.writerWithDefaultPrettyPrinter().writeValue(File("/tmp/Config.yaml"), config)
        }

        @AfterAll @JvmStatic fun teardown() {
            CoraModules.initialize()
        }
    }

    @Test fun readSimpleSaneConfig() {
        // given
        val testee = YamlConfigurationReader
        // when
        val config = testee.read(myConfig)
        // then
        assertThat(config["TextProperty"]).isEqualTo("String Value")
        assertThat(config["BooleanProperty"]).isEqualTo(true)
        assertThat(config["NullProperty"]).isNull()
        assertThat(config["MissingProperty"]).isNull()
        assertThat(config["IntProperty"]).isEqualTo(-100000)
        assertThat(config["LongProperty"]).isEqualTo(1234567890123456789L)
        assertThat(config["FloatProperty"]).isEqualTo(12.5)
        assertThat(config["DoubleProperty"]).isEqualTo(1.2E+100)
        assertThat(config["DecimalProperty"]).isEqualTo(1.2345678901234567E9)
        assertThat(config["BigIntegerProperty"]).isEqualTo(
                BigInteger("34587326450827059813745091837540193847102934861085610834761092384710875610823471092384710965")
        )
        assertThat(config["BinaryProperty"]).isEqualTo(ByteArray(256) { it.toByte() })
    }

}
