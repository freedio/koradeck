/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.module

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.TypeDirectory
import com.coradec.coradeck.dir.model.TypeFolder
import com.coradec.coradeck.dir.model.impl.*
import com.coradec.coradeck.dir.view.DirectoryView
import com.coradec.coradeck.dir.view.impl.BasicDirectoryView
import kotlin.reflect.KType

class CoraDirImpl: CoraDirAPI {
    override val rootNamespace: DirectoryNamespace = RootNamespace
    override val defaultNamespace: DirectoryNamespace = RootNamespace

    override fun getDirectoryView(path: String, root: Directory?): DirectoryView = BasicDirectoryView(path, root ?: Root)
    override fun createDirectory(namespace: DirectoryNamespace?): Directory = BasicDirectoryTemplate(namespace)
    override fun createTypeDirectory(): TypeDirectory = BasicTypeDirectoryTemplate()
    override fun <T : Any> createTypeFolder(type: KType): TypeFolder<T> = BasicTypeFolderTemplate(type)
}
