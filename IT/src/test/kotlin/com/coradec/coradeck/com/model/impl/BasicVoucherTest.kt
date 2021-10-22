/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException

internal class BasicVoucherTest {

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl())
        }
    }

    @Test fun testRegularVoucherCompletion() {
        // given
        val testee = BasicVoucher<Int>(here)
        // when
        testee.value = 3
        testee.succeed()
        // then
        assertThat(testee.value).isEqualTo(3)
    }

    @Test fun testNullableVoucherCompletion() {
        // given
        val testee = BasicVoucher<String?>(here)
        // when
        testee.value = null
        testee.succeed()
        // then
        assertThat(testee.value).isNull()
    }

    @Test fun testRegularVoucherCompletionBeforeValue() {
        // given
        val testee = BasicVoucher<Int>(here)
        // when
        val r1 = try {
            testee.succeed()
            null
        } catch (e: IllegalStateException) {
            e
        }
        // then
        assertThat(r1).isInstanceOf(IllegalStateException::class.java)
    }

    @Test fun testNullableVoucherCompletionBeforeValue() {
        // given
        val testee = BasicVoucher<String?>(here)
        // when
        val r1 = try {
            testee.succeed()
            null
        } catch (e: IllegalStateException) {
            e
        }
        // then
        assertThat(r1).isInstanceOf(IllegalStateException::class.java)
    }

    @Test fun testRegularVoucherValueWithoutCompletion() {
        // given
        val testee = BasicVoucher<Int>(here)
        // when
        testee.value = 42
        val r1 = try {
            testee.value(Timespan(10, MILLISECONDS))
        } catch (e: TimeoutException) {
            e
        }
        // then
        assertThat(r1).isInstanceOf(TimeoutException::class.java)
    }

    @Test fun testNullableVoucherValueWithoutCompletion() {
        // given
        val testee = BasicVoucher<String?>(here)
        // when
        testee.value = "Hello, World!"
        val r1 = try {
            testee.value(Timespan(10, MILLISECONDS))
        } catch (e: TimeoutException) {
            e
        }
        // then
        assertThat(r1).isInstanceOf(TimeoutException::class.java)
    }

}
