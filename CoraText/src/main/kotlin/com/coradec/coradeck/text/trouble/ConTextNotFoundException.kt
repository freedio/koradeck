package com.coradec.coradeck.text.trouble

import java.util.*

class ConTextNotFoundException(val context: String, val locale: Locale, val name: String) : TextException()
