/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.ctrl

import com.coradec.coradeck.core.util.whenTerminated
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ThreadMonitorTest {

    @Test fun terminationHook() {
        // given
        val t1 = TestThread1().apply { start() }
        val t2 = TestThread2().apply { start() }
        val t3 = TestThread3().apply { start() }
        var r0: String? = null
        var r1: String? = null
        var r2: String? = null
        var r3: String? = null
        // when
        t1.whenTerminated { r1 = "TT1 terminated." }
        t2.whenTerminated { r2 = "TT2 terminated." }
        t3.whenTerminated { r3 = "TT3 terminated." }
        t1.whenTerminated { r0 = "All threads terminated." }
        val r4 = ThreadMonitor.runningCount
        t3.join()
        println("Thread 3 terminated")
        t2.join()
        println("Thread 2 terminated")
        t1.join()
        println("Thread 1 terminated")
        Thread.sleep(100)
        // then
        assertThat(r0).isEqualTo("All threads terminated.")
        assertThat(r1).isEqualTo("TT1 terminated.")
        assertThat(r2).isEqualTo("TT2 terminated.")
        assertThat(r3).isEqualTo("TT3 terminated.")
        assertThat(r4).isEqualTo(3)
        assertThat(ThreadMonitor.runningCount).isEqualTo(0)
    }

}

class TestThread1: Thread("TT1") {
    override fun run() {
        sleep(1000)
    }
}

class TestThread2: Thread("TT2")

class TestThread3: Thread("TT3") {
    override fun run() {
        interrupt()
        sleep(1000)
    }
}
