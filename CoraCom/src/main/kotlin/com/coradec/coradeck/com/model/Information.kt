/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.core.model.Deferred
import com.coradec.coradeck.core.model.Formattable
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.Duration
import java.time.ZonedDateTime

interface Information: Formattable, Deferred {
    /** Who or what sent the information, or where does it come from. */
    val origin: Origin
    /** Session information about the information. */
    val session: Session
    /** When exactly the information was created. */
    val createdAt: ZonedDateTime
    /** As of when the information is valid.  The information will be deferred until it becomes valid. */
    val validFrom: ZonedDateTime
    /** How long the information is valid, if at all. */
    val validUpTo: ZonedDateTime

    /** Creates and returns a fresh copy of this information with the specified properties substituted. */
    fun <I: Information> copy(substitute: Map<String, Any?>): I
    /** Creates and returns a fresh copy of this information with the specified properties substituted. */
    fun <I: Information> copy(vararg substitute: Pair<String, Any?>): I
    fun wrap(origin: Origin): Notification<*>

    companion object {
        val PROP_VALIDITY = LocalProperty("Validity", Duration.ofSeconds(20))
    }
}
