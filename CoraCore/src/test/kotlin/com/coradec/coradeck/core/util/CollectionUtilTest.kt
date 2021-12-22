/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CollectionUtilTest {

    @Test fun testZipper() {
        // given:
        val s1 = listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35).asSequence()
        val s2 = listOf(1, 1, 2, 3, 5, 8, 13, 21, 34).asSequence()
        // when:
        val zap1: List<Int> = s1.zipWith(s2) { it }.filter { (a, b) -> a == b }.map { (a, _) -> a }.toList()
        val zap2: List<Int> = s2.zipWith(s1) { it }.filter { (a, b) -> a == b }.map { (a, _) -> a }.toList()
        // then:
        assertThat(zap1).containsExactly(1, 3, 5, 13, 21)
        assertThat(zap2).containsExactly(1, 3, 5, 13, 21)
    }

    @Test fun testEmptyZipper() {
        // given:
        val s1 = listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35).asSequence()
        val s2 = listOf<Int>().asSequence()
        // when:
        val zap1: List<Int> = s1.zipWith(s2) { it }.filter { (a, b) -> a == b }.map { (a, _) -> a }.toList()
        val zap2: List<Int> = s2.zipWith(s1) { it }.filter { (a, b) -> a == b }.map { (a, _) -> a }.toList()
        // then:
        assertThat(zap1).isEmpty()
        assertThat(zap2).isEmpty()
    }

}
