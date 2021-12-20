/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.ValueDirectoryEntry
import com.coradec.coradeck.dir.model.impl.BasicDirectory
import com.coradec.coradeck.dir.model.impl.BasicDirectoryNamespace
import com.coradec.coradeck.dir.model.impl.RootNamespace
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.dir.trouble.DirectoryEntryAlreadyExistsException
import com.coradec.coradeck.dir.trouble.DirectoryNotFoundException
import com.coradec.coradeck.dir.view.DirectoryView
import com.coradec.coradeck.module.model.CoraModules
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class DirectoryUT {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraDirImpl::class)
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            CoraModules.initialize()
        }
    }

    @Test fun addValueEntry() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["entry"] = "ENTRY"
        val value = directory["entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        directory.commit()
        // then
        assertThat(value).isEqualTo("ENTRY")
        assertThat(root["entry"]).isEqualTo(directory["entry"])
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(1)
    }

    @Test fun addValueEntryAsRoot() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        val result = try {
            directory[""] = "ENTRY"
            "Should have thrown DirectoryEntryAlreadyExistsException"
        } catch (e: DirectoryEntryAlreadyExistsException) {
            e
        }
        // then
        assertThat(result).isInstanceOf(DirectoryEntryAlreadyExistsException::class.java)
        val exception = result as DirectoryEntryAlreadyExistsException
        assertThat(exception.message).isEqualTo("(Path: \"\")")
        assertThat(root.count).isEqualTo(0)
        assertThat(root.countAll).isEqualTo(0)
    }

    @Test fun addDifferentEntries() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["entry"] = "ENTRY"
        directory["testdir"] = Directory()
        directory["testdir/entry"] = "OTHER"
        directory.commit()
        val r1 = directory["entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        val r2 = directory["testdir/entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(r1).isEqualTo("ENTRY")
        assertThat(r2).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(2)
        assertThat(root.countAll).isEqualTo(3)
    }

    @Test fun addDifferentEntriesWrongOrder() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["entry"] = "ENTRY"
        val result = try {
            directory["testdir/entry"] = "OTHER"
            directory["testdir"] = Directory()
            directory.commit()
            directory["testdir/entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        } catch (e: DirectoryNotFoundException) {
            e
        }
        directory.commit()
        // then
        assertThat(result).isInstanceOf(DirectoryNotFoundException::class.java)
        val exception = result as DirectoryNotFoundException
        assertThat(exception.message).isEqualTo("(Path: \"testdir/entry\")")
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(1)
    }

    @Test fun addDirectoryWithOwnNamespace() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["testdir"] = Directory(DotNamespace)
        directory["testdir.entry"] = "OTHER"
        directory.commit()
        val result = directory["testdir.entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(result).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(2)
    }

    @Test fun lookupSelfRelative() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["testdir"] = Directory()
        directory["testdir/entry"] = "OTHER"
        directory.commit()
        directory.workingPath = "testdir"
        val result = directory["./entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(result).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(2)
    }

    @Test fun lookupSelfRelativeWithDifferentNamespace() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["testdir"] = Directory(DotNamespace)
        directory["testdir.entry"] = "OTHER"
        directory.commit()
        directory.workingPath = "testdir"
        val result = directory["+entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(result).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(2)
    }

    @Test fun lookupParentRelative() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["testdir"] = Directory()
        directory["testdir/subdir"] = Directory()
        directory["testdir/entry"] = "OTHER"
        directory.commit()
        directory.workingPath = "testdir/subdir"
        val result = directory["../entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(result).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(3)
    }

    @Test fun lookupParentRelativeWithDifferentNamespace() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["testdir"] = Directory(DotNamespace)
        directory["testdir.subdir"] = Directory()
        directory["testdir.entry"] = "OTHER"
        directory.commit()
        directory.workingPath = "testdir.subdir"
        val result = directory["^entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(result).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(1)
        assertThat(root.countAll).isEqualTo(3)
    }

    @Test fun lookupBeforeCommit() {
        // given
        val root = TestRoot()
        val directory = DirectoryView("/", root)
        // when
        directory["testdir"] = Directory()
        directory["testdir/entry"] = "OTHER"
        val result = directory["testdir/entry"].let { if (it is ValueDirectoryEntry<*>) it.value else null }
        // then
        assertThat(result).isEqualTo("OTHER")
        assertThat(root.count).isEqualTo(0)
        assertThat(root.countAll).isEqualTo(0)
    }

}

object DotNamespace: BasicDirectoryNamespace(".", "+", "^")
class TestRoot: BasicDirectory(null, "", RootNamespace)
