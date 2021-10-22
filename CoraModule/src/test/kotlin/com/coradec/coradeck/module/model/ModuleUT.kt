/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.module.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD

@Execution(SAME_THREAD)
internal class ModuleUT {

    @Test fun testFindSingleModuleImpl() {
        // given
        val impl1 = TestModuleImpl1a()
        val impl2 = TestModuleImpl2a()
        val impl3 = TestModuleImpl2b()
        val impl4 = TestModuleImpl3a()
        CoraModules.initialize()
        CoraModules.register(impl1, impl2, impl3, impl4)
        // when
        val module = TestModule1()
        val result = module.impl
        // then
        assertThat(result).isEqualTo(impl1)
        assertThat(module.implementations.modules).containsExactly(impl1)
    }

    @Test fun testFindSeveralModuleImpls() {
        // given
        val impl1 = TestModuleImpl1a()
        val impl2 = TestModuleImpl2a()
        val impl3 = TestModuleImpl2b()
        val impl4 = TestModuleImpl3a()
        CoraModules.initialize()
        CoraModules.register(impl1, impl2, impl3, impl4)
        // when
        val module = TestModule2()
        val result = module.impl
        // then
        assertThat(result).isEqualTo(impl3)
        assertThat(module.implementations.modules).containsExactly(impl2, impl3)
    }

    @Test fun testDontFindModuleWithoutImplementations() {
        // given
        val impl1 = TestModuleImpl1a()
        val impl2 = TestModuleImpl2a()
        val impl3 = TestModuleImpl2b()
        CoraModules.initialize()
        CoraModules.register(impl1, impl2, impl3)
        // when
        val module = TestModule3()
        val result =
                try {
                    module.impl
                } catch (e: com.coradec.coradeck.module.trouble.MissingModuleImplementationException) {
                    e
                }
        // then
        assertThat(result).isInstanceOf(com.coradec.coradeck.module.trouble.MissingModuleImplementationException::class.java)
        val problem = result as com.coradec.coradeck.module.trouble.MissingModuleImplementationException
        assertThat(problem.message).isEqualTo("(Type: class com.coradec.coradeck.module.model.TestModule3)")
        assertThat(module.implementations.modules).isEmpty()
    }

    @Test fun testLookupPropertyOnSingleImpl() {
        // given
        val impl1 = TestModuleImpl1a()
        val impl2 = TestModuleImpl2a()
        val impl3 = TestModuleImpl2b()
        val impl4 = TestModuleImpl3a()
        CoraModules.initialize()
        CoraModules.register(impl1, impl2, impl3, impl4)
        // when
        val module = TestModule3()
        val result = module.impl
        // then
        assertThat(result.prop).isTrue()
    }

    @Test fun testLookupPropertyOnMultipleImpls() {
        // given
        val impl1 = TestModuleImpl1a()
        val impl2 = TestModuleImpl2a()
        val impl3 = TestModuleImpl2b()
        val impl4 = TestModuleImpl3a()
        CoraModules.initialize()
        CoraModules.register(impl1, impl2, impl3, impl4)
        // when
        val module = TestModule2()
        val result = module.impl
        // then
        assertThat(result.value).isEqualTo("preferred")
    }

}

interface TestModule1API: CoraModuleAPI

class TestModule1: CoraModule<TestModule1API>()

class TestModuleImpl1a : TestModule1API

interface TestModule2API: CoraModuleAPI {
    val value: String
}

class TestModule2: CoraModule<TestModule2API>() {
    val value: String get() = impl.value
}

class TestModuleImpl2a : TestModule2API {
    override val value: String = "simple"
}

class TestModuleImpl2b : TestModule2API {
    override val value: String = "preferred"
}

interface TestModule3API: CoraModuleAPI {
    val prop: Boolean
}

class TestModule3: CoraModule<TestModule3API>() {
    val prop: Boolean get() = impl.prop
}

class TestModuleImpl3a : TestModule3API {
    override val prop = true
}
