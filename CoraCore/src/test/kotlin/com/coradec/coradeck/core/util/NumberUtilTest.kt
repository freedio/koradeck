/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NumberUtilTest {

    @Test fun octalTest() {
        // given:
        // when:
        val i1 = 25.octal
        val i2 = 1234567.octal
        val i3 = 100.octal
        val i4 = -100.octal
        // then:
        assertThat(i1).isEqualTo(21)
        assertThat(i2).isEqualTo(342391)
        assertThat(i3).isEqualTo(64)
        assertThat(i4).isEqualTo(-64)
    }

}