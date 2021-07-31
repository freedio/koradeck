package com.coradec.coradeck.ctrl.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.com.trouble.RequestCancelledException
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BasicAgentTest {
    @Test fun testRoutes() {
        // given:
        val testee = object: BasicAgent() {
            @Volatile var received = 0
            init {
                addRoute(TestRouteRequest::class.java, ::testRoute)
                synchronize()
            }

            private fun testRoute(request: TestRouteRequest) {
                ++received
                request.succeed()
            }
        }
        // when:
        testee.inject(TestRouteRequest(here, testee))
        testee.inject(TestRouteRequest(here, testee)).standBy()
        // then:
        assertThat(testee.received).isEqualTo(2)
    }

    @Test fun testNoRoute() {
        // given:
        val testee = object: BasicAgent() {
            @Volatile var received = 0
        }
        // when:
        try {
            testee.inject(TestRouteRequest(here, testee)).standBy()
            // then:
            fail("Expected RequestCancelledException!")
        } catch (e: RequestCancelledException) {
            // expected that
        }
    }

    @Test fun testApproved() {
        TODO("Not yet implemented")
    }

    @Test fun testUnapproved() {
        TODO("Not yet implemented")
    }

    class TestRouteRequest(origin: Origin, recipient: Recipient): BasicRequest(origin, recipient)

    companion object {
        @BeforeAll @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl(), CoraControlImpl())
        }
    }
}