/*
 * Copyright ⓒ 2019 by Coradec GmbH. All rights reserved.
 */

package com.coradec.coradeck.com.model

enum class Severity(val severe: Boolean) {
    SUBLIMINAL(false),
    INFORMATIONAL(false),
    SEVERE(true),
    CRITICAL(true)
}
