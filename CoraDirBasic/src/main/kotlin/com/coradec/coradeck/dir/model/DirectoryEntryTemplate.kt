package com.coradec.coradeck.dir.model

interface DirectoryEntryTemplate: DirectoryEntry {
    fun real(parent: Directory?, name: String): DirectoryEntry
}
