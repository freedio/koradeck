/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir

import com.coradec.coradeck.dir.model.TypeDirectory
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.dir.view.DirectoryView
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.reflect.full.createType

class TypeDirectoryUT {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraDirImpl(), CoraTypeImpl())
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            CoraModules.initialize()
        }
    }

    @Test fun addTypeDirectory() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["types"] = TypeDirectory()
        directory.commit()
        // then
        assertThat(root["types"]).isInstanceOf(TypeDirectory::class.java)
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(1)
    }

    @Test fun addType() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        val entry = TestType1()
        // when
        val types = TypeDirectory()
        directory["types"] = types
        types += entry
        directory.commit()
        val result = root["types"]
        // then
        assertThat(result).isInstanceOf(TypeDirectory::class.java)
        val typeDir = result as TypeDirectory
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(1)
        assertThat(typeDir.types).containsExactly(entry::class.createType())
        assertThat(typeDir[TestType1::class]).containsExactly(entry)
    }

    @Test fun addTypes() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        val entry1 = TestType1()
        val entry2 = TestType2()
        // when
        val types = TypeDirectory()
        directory["types"] = types
        types += entry1
        types += entry2
        directory.commit()
        val result = root["types"]
        // then
        assertThat(result).isInstanceOf(TypeDirectory::class.java)
        val typeDir = result as TypeDirectory
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(1)
        assertThat(typeDir.types).containsExactlyInAnyOrder(entry1::class.createType(), entry2::class.createType())
        assertThat(typeDir[TestType1::class]).containsExactly(entry1)
        assertThat(typeDir[TestType2::class]).containsExactly(entry2)
        assertThat(typeDir[TestInterface1::class]).containsExactlyInAnyOrder(entry1, entry2)
    }
}

interface TestInterface1
interface TestInterface2
open class TestSuperType1
open class TestSuperType2
class TestType1: TestSuperType1(), TestInterface1
class TestType2: TestSuperType2(), TestInterface1, TestInterface2
