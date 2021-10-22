/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.module.db.ctrl.impl

import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.ctrl.impl.BasicSqlEngine
import com.coradec.coradeck.db.ctrl.impl.SqlSelection
import com.coradec.coradeck.module.model.CoraModules.register
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import com.coradec.module.db.annot.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.SQLSyntaxErrorException
import java.time.LocalDate

internal class BasicSqlEngineTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl())
            BasicSqlEngine(TestClass2::class).use { table ->
                table.assertTable()
                table.insert(TestClass2("Jane", "Doe", LocalDate.of(2000, 1, 1), 2))
                table.insert(TestClass2("Jack", "Rack", LocalDate.of(1970, 12, 31), 1))
            }
        }
    }

    @Test
    fun testTableName() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = testee.tableName
            // then:
            assertThat(result).isEqualTo("TEST_CLASS")
        }
    }

    @Test
    fun testTableName2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.tableName
            // then:
            assertThat(result).isEqualTo("TEST_CLASS2")
        }
    }

    @Test
    fun testFieldNames() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = testee.fieldNames
            // then:
            assertThat(result).hasSameElementsAs(listOf("vorname", "familienName", "geburtsdatum", "geschlecht"))
        }
    }

    @Test
    fun testFieldNames2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.fieldNames
            // then:
            assertThat(result).hasSameElementsAs(listOf("vorname", "familienName", "geburtsdatum", "geschlecht"))
        }
    }

    @Test
    fun testTableNames() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = testee.tableNames
            // then:
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun testTableNames2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.tableNames
            // then:
            assertThat(result).containsExactly("TEST_CLASS2")
        }
    }

    @Test
    fun testFields() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result =
                testee.fields.mapValues {
                    Pair(
                        it.value.first,
                        (it.value.second.singleOrNull { an -> an is Size } as Size?)?.value)
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
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result =
                testee.fields.mapValues {
                    Pair(
                        it.value.first,
                        (it.value.second.singleOrNull { an -> an is Size } as Size?)?.value)
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
    fun testColumnNames() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = testee.columnNames
            // then:
            assertThat(result).hasSameElementsAs(listOf("VORNAME", "FAMILIEN_NAME", "GEBURTSDATUM", "GESCHLECHT"))
        }
    }

    @Test
    fun testColumnNames2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.columnNames
            // then:
            assertThat(result).hasSameElementsAs(listOf("VORNAME", "FAMILIEN_NAME", "GEBURTSDATUM", "GESCHLECHT"))
        }
    }

    @Test
    fun testColumnDefinitions() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = testee.columnDefinitions.toMap()
            // then:
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    "VORNAME" to "VARCHAR(20)",
                    "FAMILIEN_NAME" to "VARCHAR(40)",
                    "GEBURTSDATUM" to "DATE",
                    "GESCHLECHT" to "TINYINT"
                )
            )
        }
    }

    @Test
    fun testColumnDefinitions2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.columnDefinitions.toMap()
            // then:
            assertThat(result).containsAllEntriesOf(
                mapOf(
                    "VORNAME" to "VARCHAR(20)",
                    "FAMILIEN_NAME" to "VARCHAR(40)",
                    "GEBURTSDATUM" to "DATE",
                    "GESCHLECHT" to "TINYINT"
                )
            )
        }
    }

    @Test
    fun testInsertRecord() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = try {
                testee.insert(TestClass("John", "Buck", LocalDate.of(1970, 1, 1), 1))
            } catch (e: SQLSyntaxErrorException) {
                e
            }
            // then:
            assertThat(result).isInstanceOf(SQLSyntaxErrorException::class.java)
            assertThat((result as SQLSyntaxErrorException).message).isEqualTo("user lacks privilege or object not found: TEST_CLASS")
        }
    }

    @Test
    fun testInsertRecord2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.insert(TestClass2("John", "Buck", LocalDate.of(1970, 1, 1), 1))
            // then:
            assertThat(result).isEqualTo(1)
        }
    }

    @Test fun testDeleteRecord() {
        // given:
        BasicSqlEngine(TestClass::class).use { testee ->
            // when:
            val result = try {
                testee.delete(where("[familienName = 'Doe']"))
            } catch (e: SQLSyntaxErrorException) {
                e
            }
            // then:
            assertThat(result).isInstanceOf(SQLSyntaxErrorException::class.java)
            assertThat((result as SQLSyntaxErrorException).message).isEqualTo("user lacks privilege or object not found: TEST_CLASS")
        }
    }

    @Test
    fun testDeleteRecord2() {
        // given:
        BasicSqlEngine(TestClass2::class).use { testee ->
            // when:
            val result = testee.delete(where("[familienName = 'Doe']"))
            // then:
            assertThat(result).isEqualTo(1)
        }
    }

    private fun where(expr: String): Selection = SqlSelection(expr)

    data class TestClass(
        val vorname: @Size(20) String,
        val familienName: @Size(40) String,
        val geburtsdatum: LocalDate,
        val geschlecht: Byte?
    )

    data class TestClass2(
        val vorname: @Size(20) String,
        val familienName: @Size(40) String,
        val geburtsdatum: LocalDate,
        val geschlecht: Byte?
    )

}
