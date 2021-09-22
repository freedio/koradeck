/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

enum class Severity(val severe: Boolean) {
    SUBLIMINAL(false),
    INFORMATIONAL(false),
    SEVERE(true),
    CRITICAL(true)
}
