/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class CacheQueueTest {

    @Test fun normalOperations1() {
        // given
        val testee = CacheQueue<Int>()
        // when
        testee.add(25_000_000)
        testee.offer(30_000_000)
        testee.addAll(1..10_000)
        val it = testee.iterator()
        // then
        assertThat(testee).isNotEmpty
        assertThat(testee).hasSize(10_002)
        assertThat(testee.contains(1)).isTrue()
        assertThat(testee.contains(0)).isFalse()
        assertThat(testee).contains(25_000_000, 30_000_000, 1_000, 9_999)
        assertThat(it.hasNext() && it.next() == 25_000_000).isTrue()
        assertThat(it.hasNext() && it.next() == 30_000_000).isTrue()
        for (i in 1..10_000) assertThat(it.hasNext() && it.next() == i).isTrue()
        assertThat(it.hasNext()).isFalse()
        assertThat(testee.removeAll(1..10_000)).isTrue()
        assertThat(testee.retainAll(1..10_000)).isTrue()
        assertThat(testee).isEmpty()
    }

    @Test fun normalOperations2() {
        // given
        val testee = CacheQueue<Int>()
        // when
        testee.add(25_000_000)
        testee.offer(30_000_000)
        testee.addAll(1..10_000)
        val r1 = testee.remove()
        val r2 = testee.poll()
        // then
        assertThat(r1).isEqualTo(25_000_000)
        assertThat(r2).isEqualTo(30_000_000)
        assertThat(testee.containsAll((1..10_000).toList())).isTrue()
        for (i in 1..10_000) assertThat(testee.poll()).isEqualTo(i)
        assertThat(testee.poll()).isNull()
        assertThatThrownBy { testee.remove() }.isInstanceOf(NoSuchElementException::class.java)
    }

    @Test fun normalOperations3() {
        // given
        val testee = CacheQueue<Int>()
        // when
        testee.add(25_000_000)
        testee.offer(30_000_000)
        testee.addAll((1..100_000).toList())
        val r1 = testee.remove()
        val r2 = testee.poll()
        // then
        assertThat(r1).isEqualTo(25_000_000)
        assertThat(r2).isEqualTo(30_000_000)
        for (i in 1..100_000) assertThat(testee.poll()).isEqualTo(i)
        assertThat(testee.poll()).isNull()
        assertThatThrownBy { testee.remove() }.isInstanceOf(NoSuchElementException::class.java)
    }

    @Test fun normalOperations4() {
        // given
        val testee = CacheQueue<Int>()
        // when
        testee.add(25_000_000)
        testee.offer(30_000_000)
        testee.addAll(1..100_000)
        val r1 = testee.element()
        val r3 = testee.remove(25_000_000)
        val r2 = testee.peek()
        testee.add(0, 99_999_999)
        testee.add(-3, 99_999_999)
        val r4 = testee.indexOf(99_999_999)
        testee.remove(99_999_999)
        val r5 = testee.indexOf(99_999_999)
        // then
        assertThat(r1).isEqualTo(25_000_000)
        assertThat(r2).isEqualTo(30_000_000)
        assertThat(r3).isTrue()
        assertThat(testee.remove(25_000_000)).isFalse()
        assertThat(r4).isEqualTo(0)
        assertThat(r5).isEqualTo(99_998)
        testee.remove()
        testee.clear()
        assertThat(testee.peek()).isNull()
        assertThatThrownBy { testee.element() }.isInstanceOf(NoSuchElementException::class.java)
    }

    @Test fun underPressure() {
        // given
        val testee = CacheQueue<Huge>()
        // when
        (1..50_000).forEach { testee.add(Huge(it)) }
        // then
        println(testee.size)
        assertThat(testee.size).isLessThan(25_000)
    }

    @Test fun notUnderPressure() {
        // given
        val testee = CacheQueue<Huge>()
        // when
        (1..1000).forEach { testee.add(Huge(it)) }
        // then
        assertThat(testee.size).isEqualTo(1000)
    }

    val huge = ByteArray(1_000_000) { it.and(0xff).toByte() }
    inner class Huge(val index: Int) {
        val payload = huge.clone()
    }

}
