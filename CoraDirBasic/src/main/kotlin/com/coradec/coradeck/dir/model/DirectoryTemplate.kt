/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

interface DirectoryTemplate: Directory {
    /** Returns a real directory for the template. */
    fun real(parent: Directory?, name: String, namespace: DirectoryNamespace): Directory
    /** Retursn the specified (real) name with the trailing path separator. */
    fun nameWithSeparator(name: Path): String
}
