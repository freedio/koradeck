/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.impl

import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.com.trouble.NotificationRejectedException
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.ctrl.trouble.CommandNotApprovedException
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BasicAgentTest {
    val IMMEX = CoraControl.IMMEX
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
        testee.accept(TestRouteRequest(here))
        testee.accept(TestRouteRequest(here)).standby()
        Thread.sleep(100)
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
            testee.accept(TestRouteRequest(here)).standby()
            // then:
            fail("Expected RequestCancelledException!")
        } catch (e: NotificationRejectedException) {
            // expected that
        }
    }

    @Test fun testApproved() {
        // given:
        var received = 0
        class TestRouteCommand(origin: Origin): BasicCommand(origin) {
            override fun execute() {
                ++received
                succeed()
            }
        }

        val testee = object: BasicAgent() {
            init {
                approve(TestRouteCommand::class)
                synchronize()
            }
        }
        // when:
        testee.accept(TestRouteCommand(here))
        testee.accept(TestRouteCommand(here)).standby()
        Thread.sleep(100)
        // then:
        assertThat(received).isEqualTo(2)
    }

    @Test fun testUnapproved() {
        // given:
        var received = 0
        class TestRouteCommand(origin: Origin): BasicCommand(origin) {
            override fun execute() {
                ++received
                succeed()
            }
        }

        val testee = BasicAgent()
        // when:
        try {
            testee.accept(TestRouteCommand(here)).standby()
            // then:
            fail("Expected CommandNotApprovedException!")
        } catch (e: CommandNotApprovedException) {
            // expected that
        }
    }

    class TestRouteRequest(origin: Origin): BasicRequest(origin)

    companion object {
        @BeforeAll @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl(), CoraControlImpl())
        }
    }
}