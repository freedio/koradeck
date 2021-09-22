/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class BasicDirectoryViewTest {

    @Disabled @TestFactory fun testRealPath() = listOf<DynamicTest>(
            DynamicTest.dynamicTest("Fails") {
                throw RuntimeException("x")
            }
    )

}
