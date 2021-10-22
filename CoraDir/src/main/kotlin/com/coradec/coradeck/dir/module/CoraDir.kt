/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.module

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.TypeDirectory
import com.coradec.coradeck.dir.model.TypeFolder
import com.coradec.coradeck.dir.view.DirectoryView
import com.coradec.coradeck.module.model.CoraModule
import kotlin.reflect.KType

object CoraDir: CoraModule<CoraDirAPI>() {
    /** The root directory namespace. */
    val rootNamespace: DirectoryNamespace get() = impl.rootNamespace
    /** The default namespace. */
    val defaultNamespace: DirectoryNamespace get() = impl.defaultNamespace

    /** Creates or retrieves a directory view for the specified path with the specified root directory (default: real Root). */
    fun getDirectoryView(path: String, root: Directory? = null): DirectoryView = impl.getDirectoryView(path, root)
    /** Creates a raw directory template (optionally with its own namespace). */
    fun createDirectory(namespace: DirectoryNamespace?): Directory = impl.createDirectory(namespace)
    /** Creates a raw type directory template. */
    fun createTypeDirectory(): TypeDirectory = impl.createTypeDirectory()
    /** Creates a raw type folder template for the specified type. */
    fun <T: Any> createTypeFolder(type: KType): TypeFolder<T> = impl.createTypeFolder<T>(type)
}
