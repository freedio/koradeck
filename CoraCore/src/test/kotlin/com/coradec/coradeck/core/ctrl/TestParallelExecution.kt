/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.ctrl

import org.junit.jupiter.api.Test

class TestParallelExecution {

    @Test fun test1() {
        println("test1 running...")
        println(Thread.currentThread().contextClassLoader.getResource("junit-platform.properties"))
        println("Class path: " + System.getProperty("java.class.path"))
        Thread.sleep(5000)
        println("test1 finished...")
    }

    @Test fun test2() {
        println("test2 running...")
        Thread.sleep(5000)
        println("test2 finished...")
    }

    @Test fun test3() {
        println("test3 running...")
        Thread.sleep(5000)
        println("test3 finished...")
    }

    @Test fun test4() {
        println("test4 running...")
        Thread.sleep(5000)
        println("test4 finished...")
    }

    @Test fun test5() {
        println("test5 running...")
        Thread.sleep(5000)
        println("test5 finished...")
    }

    @Test fun test6() {
        println("test6 running...")
        Thread.sleep(5000)
        println("test6 finished...")
    }
}
