/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.module

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.TypeDirectory
import com.coradec.coradeck.dir.model.TypeFolder
import com.coradec.coradeck.dir.view.DirectoryView
import com.coradec.coradeck.module.model.CoraModuleAPI
import kotlin.reflect.KType

interface CoraDirAPI: CoraModuleAPI {
    /** The root directory namespace. */
    val rootNamespace: DirectoryNamespace
    /** The default namespace. */
    val defaultNamespace: DirectoryNamespace

    /** Creates or retrieves a directory view for the specified path with the specified root directory (default: real Root). */
    fun getDirectoryView(path: String, root: Directory?): DirectoryView
    /** Creates a raw directory template. */
    fun createDirectory(namespace: DirectoryNamespace?): Directory
    /** Creates a raw type directory template. */
    fun createTypeDirectory(): TypeDirectory
    /** Creates a raw type folder template for the specified type. */
    fun <T: Any> createTypeFolder(type: KType): TypeFolder<T>
}
