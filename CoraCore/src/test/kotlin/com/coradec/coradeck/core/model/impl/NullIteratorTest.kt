package com.coradec.coradeck.core.model.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NullIteratorTest {

    @Test fun test() {
        // given:
        val testee = NullIterator<String>()
        // when:
        val r1 = testee.hasNext()
        val r2 = try {
            testee.next()
        } catch (e: NoSuchElementException) {
            null
        }
        // then:
        assertThat(r1).isFalse()
        assertThat(r2).isNull()
    }
}