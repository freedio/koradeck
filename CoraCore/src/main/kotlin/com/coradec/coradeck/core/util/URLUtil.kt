/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import java.net.URL

val URL.lines: List<String> get() = openStream().bufferedReader().readLines()
