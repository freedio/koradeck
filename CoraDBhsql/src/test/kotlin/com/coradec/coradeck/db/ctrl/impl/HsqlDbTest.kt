/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.ctrl.impl

import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.bus.module.CoraBusImpl
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.Files
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.core.util.toPath
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.db.annot.Generated
import com.coradec.coradeck.db.annot.Indexed
import com.coradec.coradeck.db.annot.Primary
import com.coradec.coradeck.db.annot.Size
import com.coradec.coradeck.db.com.OpenTableVoucher
import com.coradec.coradeck.db.ctrl.impl.SqlSelection.Companion.where
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.impl.BasicColumnDefinition
import com.coradec.coradeck.db.model.impl.HsqlDatabase
import com.coradec.coradeck.db.module.CoraDbHsql
import com.coradec.coradeck.db.util.generate
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.model.Password
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URI
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

internal class HsqlDbTest {

    companion object {
        lateinit var db1: Database
        lateinit var db2: Database
        lateinit var database1: HsqlDatabase
        lateinit var database2: HsqlDatabase

        @Suppress("unused")
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(
                CoraConfImpl(),
                CoraComImpl(),
                CoraTextImpl(),
                CoraTypeImpl(),
                CoraDirImpl(),
                CoraControlImpl(),
                CoraBusImpl(),
                CoraDbHsql()
            )
            val log = CoraCom.log
            log.debug("@0")
            Files.deleteTree("/tmp/dbtest".toPath())
            Files.deleteTree("/tmp/dbtest2".toPath())
            log.debug("@1")
            database1 = HsqlDatabase(URI("jdbc:hsqldb:file:/tmp/dbtest/db"), "sa", Password(""))
            log.debug("@2")
            CoraBus.applicationBus.add("hsqlDB1", database1.memberView).standby()
            log.debug("@3")
            log.debug("@4")
            database1.accept(OpenTableVoucher(here,TestClass::class)).standby()
            log.debug("@5")
            database1.accept(OpenTableVoucher(here, TestClass2::class)).content.value.let { table ->
                log.debug("@5.0")
                Thread.sleep(500)
                table += TestClass2("Jane", "Doe", LocalDate.of(2000, 1, 1), 2)
                log.debug("@5.1")
                table += TestClass2("Jack", "Daniels", LocalDate.of(1864, 4, 24), 1)
                log.debug("@5.2")
            }
            log.debug("@6")
            database1.accept(OpenTableVoucher(here, TestClassWithCurrency::class)).standby()
            log.debug("@7")
            log.debug("Test suite initialized.")
            log.debug("@8")
            relax()
            database2 = HsqlDatabase(URI("jdbc:hsqldb:file:/tmp/dbtest2/db"), "sa", Password(""))
            CoraBus.applicationBus.add("hsqlDB2", database2.memberView).standby()
            database2.accept(OpenTableVoucher(here, TestClass::class)).standby()
            database2.accept(OpenTableVoucher(here, TestClass2::class)).content.value.let { table ->
                Thread.sleep(500)
                table += TestClass2("Jane", "Doe", LocalDate.of(2000, 1, 1), 2)
                table += TestClass2("Jack", "Daniels", LocalDate.of(1864, 4, 24), 1)
            }
            db1 = database1.databaseView
            db2 = database2.databaseView
        }

        @AfterAll
        @JvmStatic fun tearDown() {
            val log = CoraCom.log
            log.debug("Tear down.")
            database1.detach().standby()
            database2.detach().standby()
            Thread.sleep(1000)
            log.debug("Torn down.")
        }
    }

    @Test
    fun testName() {
        // given:
        db1.getTable(TestClass::class).let { testee ->
            // when:
            val result = testee.recordName
            // then:
            assertThat(result).isEqualTo("com.coradec.coradeck.db.ctrl.impl.HsqlDbTest.TestClass")
        }
    }

    @Test
    fun testName2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.recordName
            // then:
            assertThat(result).isEqualTo("com.coradec.coradeck.db.ctrl.impl.HsqlDbTest.TestClass2")
        }
    }

    @Test
    fun testTableName() {
        // given:
        db1.getTable(TestClass::class).let { testee ->
            // when:
            val result = testee.tableName
            // then:
            assertThat(result).isEqualTo("TEST_CLASS")
        }
    }

    @Test
    fun testTableName2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.tableName
            // then:
            assertThat(result).isEqualTo("TEST_CLASS2")
        }
    }

    @Test
    fun testFieldNames() {
        // given:
        db1.getTable(TestClass::class).let { testee ->
            // when:
            val result = testee.fieldNames.toList()
            // then:
            assertThat(result).hasSameElementsAs(listOf("vorname", "familienName", "geburtsdatum", "geschlecht"))
        }
    }

    @Test
    fun testFieldNames2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.fieldNames.toList()
            // then:
            assertThat(result).hasSameElementsAs(listOf("vorname", "familienName", "geburtsdatum", "geschlecht", "name"))
        }
    }

    @Test
    fun testFields() {
        // given:
        db1.getTable(TestClass::class).let { testee ->
            // when:
            val result = testee.fields.mapValues {
                Pair(it.value.classifier as KClass<*>, it.value.findAnnotation<Size>()?.value)
            }
            // then:
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    "familienName" to Pair(String::class, 40),
                    "geburtsdatum" to Pair(LocalDate::class, null),
                    "geschlecht" to Pair(Byte::class, null),
                    "vorname" to Pair(String::class, 20)
                )
            )
        }
    }

    @Test
    fun testFields2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.fields.mapValues {
                    Pair(it.value.classifier as KClass<*>, it.value.findAnnotation<Size>()?.value)
                }
            // then:
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    "familienName" to Pair(String::class, 40),
                    "geburtsdatum" to Pair(LocalDate::class, null),
                    "geschlecht" to Pair(Byte::class, null),
                    "vorname" to Pair(String::class, 20),
                    "name" to Pair(String::class, 60)
                )
            )
        }
    }

    @Test
    fun testColumnNames() {
        // given:
        db1.getTable(TestClass::class).let { testee ->
            // when:
            val result = testee.columnNames.toList()
            // then:
            assertThat(result).hasSameElementsAs(listOf("VORNAME", "FAMILIEN_NAME", "GEBURTSDATUM", "GESCHLECHT"))
        }
    }

    @Test
    fun testColumnNames2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.columnNames.toList()
            // then:
            assertThat(result).hasSameElementsAs(listOf("VORNAME", "FAMILIEN_NAME", "GEBURTSDATUM", "GESCHLECHT", "NAME"))
        }
    }

    @Test
    fun testColumnDefinitions() {
        // given:
        db1.getTable(TestClass::class).let { testee ->
            // when:
            val result = testee.columnDefinitions.toMap()
            // then:
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    "VORNAME" to BasicColumnDefinition("VARCHAR(20)"),
                    "FAMILIEN_NAME" to BasicColumnDefinition("VARCHAR(40)"),
                    "GEBURTSDATUM" to BasicColumnDefinition("DATE"),
                    "GESCHLECHT" to BasicColumnDefinition("TINYINT", true)
                )
            )
        }
    }

    @Test
    fun testColumnDefinitions2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.columnDefinitions.toMap()
            // then:
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    "VORNAME" to BasicColumnDefinition("VARCHAR(20)"),
                    "FAMILIEN_NAME" to BasicColumnDefinition("VARCHAR(40)"),
                    "GEBURTSDATUM" to BasicColumnDefinition("DATE"),
                    "GESCHLECHT" to BasicColumnDefinition("TINYINT", true),
                    "NAME" to BasicColumnDefinition("VARCHAR(60)")
                )
            )
        }
    }

    @Test
    fun testInsertRecord3() {
        // given:
        try {
            db1.getTable(TestClass3::class).let { testee ->
                // when:
                    testee += TestClass3("John", "Buck", LocalDate.of(1970, 1, 1), 1)
            }
            // then: fails
            fail("Expected the request to fail!")
        } catch (e: RequestFailedException) {
            // then:
            assertThat(e).hasCauseInstanceOf(MemberNotFoundException::class.java)
            assertThat(e).hasMessage("(Cause: com.coradec.coradeck.bus.trouble.MemberNotFoundException: (MemberName: \"COM_CORADEC_CORADECK_DB_CTRL_IMPL_HSQL_DB_TEST_TEST_CLASS3\"))")
        }
    }

    @Test
    fun testInsertRecord2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.insert(TestClass2("John", "Buck", LocalDate.of(1970, 1, 1), 1))
            // then:
            assertThat(result).isEqualTo(1)
        }
    }

    @Test
    fun testDeleteRecord3() {
        // given:
        // when:
        try {
            db1.getTable(TestClass3::class).let { testee ->
                testee -= where("[familienName = 'Doe']")
            }
            // then: fails
            fail("Expected the request to fail!")
        } catch (e: RequestFailedException) {
            // then
            assertThat(e).hasCauseInstanceOf(MemberNotFoundException::class.java)
            assertThat(e).hasMessage("(Cause: com.coradec.coradeck.bus.trouble.MemberNotFoundException: (MemberName: \"COM_CORADEC_CORADECK_DB_CTRL_IMPL_HSQL_DB_TEST_TEST_CLASS3\"))")
        }
    }

    @Test
    fun testDeleteRecord2() {
        // given:
        db1.getTable(TestClass2::class).let { testee ->
            // when:
            val result = testee.delete(where("[familienName = 'Doe']"))
            // then:
            assertThat(result).isEqualTo(1)
        }
    }

    @Test fun testCurrency() {
        // given:
        val francs = Currency.getInstance("CHF")
        val table = db1.getTable(TestClassWithCurrency::class)
        table += TestClassWithCurrency(3.1415926, francs)
        // when:
        val record = table.all.single()
        // then:
        assertThat(record.amount).isEqualTo(3.1415926)
        assertThat(record.currency).isEqualTo(francs)
    }

    @Test fun testReset() {
        // given:
        // when:
        db2.reset()
        // then:
        try {
            db2.getTable(TestClass::class).let { testee ->
                // when:
                testee += TestClass("John", "Buck", LocalDate.of(1970, 1, 1), 1)
            }
            // then: fails
            fail("Expected the request to fail!")
        } catch (e: RequestFailedException) {
            // then:
            assertThat(e).hasCauseInstanceOf(MemberNotFoundException::class.java)
            assertThat(e).hasMessage("(Cause: com.coradec.coradeck.bus.trouble.MemberNotFoundException: (MemberName: \"COM_CORADEC_CORADECK_DB_CTRL_IMPL_HSQL_DB_TEST_TEST_CLASS\"))")
        }
    }

    data class TestClass(
        val vorname: @Size(20) String,
        val familienName: @Size(40) String,
        val geburtsdatum: @Indexed LocalDate,
        val geschlecht: Byte?
    )

    data class TestClass2(
        val vorname: @Size(20) String,
        val familienName: @Size(40) String,
        val geburtsdatum: LocalDate,
        val geschlecht: Byte?
    ) {
        val name: @Primary @Generated("VORNAME || ' ' || FAMILIEN_NAME", true) @Size(60) String
            get() = generate("$vorname $familienName")
    }

    data class TestClass3(
        val vorname: @Size(20) String,
        val familienName: @Size(40) String,
        val geburtsdatum: LocalDate,
        val geschlecht: Byte?
    )

    data class TestClassWithCurrency(
        val amount: Double,
        val currency: Currency
    )

}
