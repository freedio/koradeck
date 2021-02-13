/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl

/**
 *  A parser to read a configuration in the .properties or Unix .conf file style.
 *
 *  This parser allows to customize
 *  - the escape character (defaut \)
 *  - the key-value delimiter set (default :=␣)
 *  - the comment characters (default #!)
 *  - the beginquotes and endquotes (character position in endquote refers to same position in beginquotes) (default
 *    " ' both)
 *
 *  This parser has all advanced parsing capabilities of a modern line parser, namely:
 *  - character escapes: \a, \e, \f, \n, \r, \', \"
 *  - octal escapes \0 .. \377
 *  - hexadecimal escapes \x0 .. \XFf
 *  - unicode escapes \u0000 .. \uffff
 *  - quoted regions (in which special characters lose their function and become normal letters) using " and '
 *  - soft line wrapping (\ at the end of the line, swallowing indent on the next line)
 *  - comments (everything after an unescaped # will be treated as comment)
 *  - different key-value-separators
 *
 *  Note that
 *  - the escape character will be swallowed unless escaped: a\tb becomes a       b
 *  - the beginquote and endquote characters will be swallowed unless escaped: abc"def"ghi becomes abcdefghi
 */
class AdvancedLineParser(
        private val delimiters: String = "= :",
        private val escape: Char = '\\',
        private val comments: String = "#!",
        private val beginquotes: String = "\"'",
        private val endquotes: String = "\"'"
) {
    fun parse(input: List<String>): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()
        val lineCollector = StringBuilder()
        val key = StringBuilder()
        val value = StringBuilder()
        val escapeBuffer = StringBuilder()
        var collector = key
        var escaped = false
        var inQuote = -1
        var inUniEscape = false
        var inOctEscape = false
        var inHexEscape = false
        fun escaped(c: Char): String {
            escaped = false
            val unesc = "aefnrt'\"0\\#"
            val esc = "\u0007\u001e\u000c\n\r\t'\"\u0000\\#"
            val chix = unesc.indexOf(c)
            return if (chix != -1) "${esc[chix]}" else "\\$c"
        }

        fun add(c: Char) {
            if (escaped) collector.append(escaped(c)) else collector.append(c)
        }

        fun processEscape(c: Char, radix: Int, maxLength: Int, limit: Int): Boolean {
            val v = "0123456789abcdef".indexOf(c.toLowerCase())
            fun addEsc(): Boolean {
                collector.append(escapeBuffer.insert(0, '0').toString().toInt(radix).toChar())
                escapeBuffer.clear()
                inUniEscape = false
                inOctEscape = false
                inHexEscape = false
                return false
            }
            if (v < 0 || v >= radix || escapeBuffer.length == maxLength) return addEsc()
            return if (escapeBuffer.append(c).toString().toInt(radix) >= limit) {
                escapeBuffer.deleteCharAt(escapeBuffer.length - 1)
                addEsc()
            } else true
        }

        fun nop() {
        }

        input.forEach { line ->
            if (line.endsWith('\\')) lineCollector.append(line.substringBeforeLast('\'').trim())
            else {
                lineCollector.append(line.trim())
                var skipRest = false
                lineCollector.toString().forEach { c ->
                    if (!skipRest)
                        when {
                            inUniEscape && processEscape(c, 16, 4, 65536) -> nop()
                            inOctEscape && processEscape(c, 8, 3, 256) -> nop()
                            inHexEscape && processEscape(c, 16, 2, 256) -> nop()
                            !escaped && inQuote != -1 && c == endquotes[inQuote] -> inQuote = -1
                            else -> when (c) {
                                escape -> escaped = true
                                in comments -> if (!escaped && inQuote == -1) skipRest = true else add(c)
                                in beginquotes -> if (!escaped) add(c) else inQuote = beginquotes.indexOf(c)
                                in delimiters ->
                                    if (collector == key && !escaped && inQuote == -1) collector = value
                                    else if (collector.isNotEmpty()) add(c)
                                else -> if (escaped) when (c) {
                                    'u', 'U' -> {
                                        inUniEscape = true
                                        escaped = false
                                    }
                                    'x', 'X' -> {
                                        inHexEscape = true
                                        escaped = false
                                    }
                                    '0', '1', '2', '3', '4', '5', '6', '7' -> {
                                        inOctEscape = true
                                        escaped = false
                                        escapeBuffer.append(c)
                                    }
                                    else -> add(c)
                                } else add(c)
                            }
                        }
                }
                if (key.isNotEmpty()) result[key.toString()] = value.toString().trim(' ')
                key.clear()
                value.clear()
                collector = key
                lineCollector.clear()
            }
        }
        return result
    }
}
