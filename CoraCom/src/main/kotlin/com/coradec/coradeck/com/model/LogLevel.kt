/*
 * Copyright ⓒ 2017−2020 by Coradec GmbH. All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.model.Severity.CRITICAL
import com.coradec.coradeck.com.model.Severity.INFORMATIONAL
import com.coradec.coradeck.com.model.Severity.SEVERE
import com.coradec.coradeck.com.model.Severity.SUBLIMINAL

enum class LogLevel(val severity: Severity, val letter: Char, val abbrev: String) {
    ALL(SUBLIMINAL, '*', "ANY"), // if you want to see everything in the log, regardless of how irrelevant
    INFRA(SUBLIMINAL, 'Q', "INFRA"), // reserved for lowest-level infrastructure (CentralMessageQueue et al.)
    SUBTRACE(SUBLIMINAL, 'S', "SUBTR"), // very low level info, but still not yet infrastructure
    TRACE(SUBLIMINAL, 'T', "TRACE"), // what you don't even want to see normally while debugging
    DEBUG(SUBLIMINAL, 'D', "DEBUG"), // what you want to see only during in-depth problem analysis
    DETAIL(SUBLIMINAL, 'L', "DTAIL"), // tech info you want to see during development
    INFORMATION(INFORMATIONAL, 'I', "INFO"), // what you want to see during problem analysis
    ALERT(INFORMATIONAL, 'A', "ALERT"), // for potential problems on a technical level
    WARNING(SEVERE, 'W', "WARN"), // for real problems which are non severe
    ERROR(SEVERE, 'E', "ERROR"), // for real severe problems, which are not fatal
    FATAL(CRITICAL, 'F', "FATAL"), // for problems that cause the system to fail as a whole
    NONE(CRITICAL, '-', "NONE"); // if you don't want to see ANY logging

    infix fun atLeast(logLevel: LogLevel): Boolean = ordinal >= logLevel.ordinal
}
