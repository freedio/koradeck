package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Body
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.Head
import com.coradec.coradeck.dir.model.Path

open class BasicDirectoryNamespace(
        private val pathSeparator: String = "/",
        private val selfReference: String = "./",
        private val parentReference: String = "../"
) : DirectoryNamespace {
    private val pathSeparatorRegex = Regex(escape(pathSeparator))
    private val selfReferenceRegex = Regex("^(${escape(selfReference)})*")
    private val parentReferenceRegex = Regex("^${escape(parentReference)}")
    private val leadingPathSeparatorsRegex = Regex("^${escape(pathSeparator)}*")

    private fun escape(unescaped: String): String = unescaped
            .replace(".", "\\.")
            .replace("^", "\\^")
            .replace("+", "\\+")
            .replace("*", "\\*")

    override fun concat(path1: String, path2: String): String = "$path1$pathSeparator$path2"
    override fun isAbsolute(path: String): Boolean = path.isEmpty() || path.startsWith(pathSeparator)
    override fun makeRelative(path: String): String = path.replace(leadingPathSeparatorsRegex, "")
    override fun isSelfRelative(path: String): Boolean = path.startsWith(selfReference)
    override fun removeSelfReference(path: String): String = path.replace(selfReferenceRegex, "")
    override fun isParentRelative(path: String): Boolean = path.startsWith(parentReference)
    override fun removeParentReference(path: String): String = path.replace(parentReferenceRegex, "")
    override fun isName(path: String): Boolean = pathSeparatorRegex !in path
    override fun nameWithSeparator(name: String): Path = name + pathSeparator

    override fun split(path: Path): Pair<Head, Body> = path
            .replace("\\/", BKSLSL_REPL)
            .split(pathSeparatorRegex, 2).let {
                if (it.size != 2) throw IllegalArgumentException("Path «$path» cannot be split with seaprator ‹$pathSeparator›!")
                Pair(it[0].replace(BKSLSL_REPL, "\\/"), it[1].replace(BKSLSL_REPL, "\\/"))
            }

    override fun splitIfPrefixedWith(name: String, path: Path): Pair<Head, Body>? = path
            .replace("\\/", BKSLSL_REPL)
            .split(pathSeparatorRegex, 2).let {
                if (it.size != 2 || it[0] != name) null
                else Pair(it[0].replace(BKSLSL_REPL, "\\/"), it[1].replace(BKSLSL_REPL, "\\/"))
            }

    companion object {
        private const val BKSLSL_REPL = "<<backslash-slash>>"
    }
}
