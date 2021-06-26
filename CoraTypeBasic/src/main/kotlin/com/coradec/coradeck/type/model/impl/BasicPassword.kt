package com.coradec.coradeck.type.model.impl

import com.coradec.coradeck.type.model.Password
import kotlin.experimental.xor

class BasicPassword(cleartext: String) : Password {
    val encoded = encode(cleartext)
    override val decoded: String get() = String(decode(encoded))

    // TODO too primitive, find a better method
    private fun encode(cleartext: String): ByteArray = cleartext.toByteArray().map { it xor 0x55 }.toByteArray()
    private fun decode(scrambled: ByteArray): ByteArray = scrambled.map { it xor 0x55 }.toByteArray()
}
