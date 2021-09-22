/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

interface DirectoryEntryTemplate: DirectoryEntry {
    fun real(parent: Directory?, name: String): DirectoryEntry
}
