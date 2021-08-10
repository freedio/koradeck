/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.impl.BasicMessage
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class CEMSUT {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTypeImpl(), CoraComImpl(), CoraTextImpl(), CoraControlImpl())
        }
        @AfterAll
        @JvmStatic
        fun cleanup() {
            CoraModules.initialize()
        }
    }

    @Test fun testMessageInjectionCEMS() {
        // given
        val agent = TestAgent1()
        val message = TestMessage1(here, agent)
        // when
        CEMS.inject(message)
        CEMS.onQueueEmpty {
            // then
            assertThat(agent.gotMessage).isTrue()
        }
    }

    @Test fun testMessageInjectionAgent() {
        // given
        val agent = TestAgent1()
        val message = TestMessage1(here, agent)
        // when
        agent.inject(message)
        CEMS.onQueueEmpty {
            // then
            assertThat(agent.gotMessage).isTrue()
        }
    }

    class TestMessage1(origin: Origin, target: Recipient?) : BasicMessage(origin, target = target)

    class TestAgent1: BasicAgent() {
        var gotMessage: Boolean = false
    }

}
