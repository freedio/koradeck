package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.com.model.impl.Syslog
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class BasicAgentPoolTest {

    @Test fun testRelaxedPool() {
        // given:
        val result = TestEvaluator()
        val testee = CoraControl.createAgentPool(0, 20) { TestAgent() }
        // when:
        IntRange(1, 100).forEach { testee.inject(TestRequest(here, result, it)) }
        // then:
        Thread.sleep(100)
        assertThat(result.resultList.size).isEqualTo(100)
        assertThat(result.executors.size).isGreaterThan(1)
    }

    @Test fun testPoolUnderPressure() {
        // given:
        val result = TestEvaluator()
        val testee = CoraControl.createAgentPool(0, 20) { TestAgent() }
        // when:
        IntRange(1, 10000).forEach { testee.inject(TestRequest(here, result, it)) }
        // then:
        Thread.sleep(1000)
        assertThat(result.resultList.size).isEqualTo(10000)
        assertThat(result.executors.size).isGreaterThan(1)
    }

    @Test fun testFailingPool() {
        // given:
        val result = TestEvaluator()
        val testee = CoraControl.createAgentPool(0, 20) { FailingTestAgent() }
        // when:
        IntRange(1, 1000).forEach { testee.inject(TestRequest(here, result, it)) }
        // then:
        Thread.sleep(100)
        assertThat(result.resultList.size).isEqualTo(995)
        assertThat(result.executors.size).isGreaterThan(1)
    }

    @Test fun testPoolWithFailingMessage() {
        // given:
        val result = TestEvaluator()
        val testee = CoraControl.createAgentPool(0, 20) { TestAgent() }
        // when:
        IntRange(1, 1000).forEach { testee.inject(FailingTestRequest(here, result, it)) }
        // then:
        Thread.sleep(100)
        assertThat(result.resultList.size).isEqualTo(995)
        assertThat(result.executors.size).isGreaterThan(1)
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl(), CoraControlImpl())
        }

        open class TestRequest(origin: Origin, val evaluator: TestEvaluator, val id: Int): BasicCommand(origin) {
            override val copy: BasicCommand get() = TestRequest(origin, evaluator, id)

            override fun execute() {
                Thread.sleep(10)
                Syslog.debug("Executing TestRequest $id.")
                evaluator += id
                evaluator.executedBy(id, Thread.currentThread().name)
                succeed()
            }

        }

        class FailingTestRequest(origin: Origin, evaluator: TestEvaluator, id: Int): TestRequest(origin, evaluator, id) {
            override val copy: BasicCommand get() = FailingTestRequest(origin, evaluator, id)

            override fun execute() {
                if (id % 200 == 0) fail() else super.execute()
            }

        }

        class TestAgent: BasicAgent() {
            init {
                approve(TestRequest::class, FailingTestRequest::class)
            }

            override fun <I : Information> inject(message: I): I = super.inject(message).also {
                Syslog.debug("Injected TestRequest ${(message as TestRequest).id}.")
            }
        }

        class FailingTestAgent: BasicAgent() {
            init {
                approve(TestRequest::class, FailingTestRequest::class)
            }

            override fun <I : Information> inject(message: I): I =
                if (++nth == nthfail) {
                    nth = 0
                    throw RuntimeException("I failed")
                } else super.inject(message).also { Syslog.debug("Injected TestRequest ${(message as TestRequest).id}.") }

            companion object {
                private val nthfail = 200
                private var nth = 0
            }
        }

        class TestEvaluator {
            val resultList = CopyOnWriteArrayList<Int>()
            val executors = ConcurrentHashMap<String, MutableList<Int>>()

            operator fun plusAssign(id: Int) { resultList += id }
            fun executedBy(id: Int, name: String) {
                executors.computeIfAbsent(name) { CopyOnWriteArrayList() } += id
            }
        }
    }

}