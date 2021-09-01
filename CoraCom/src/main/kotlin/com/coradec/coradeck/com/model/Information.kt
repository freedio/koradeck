/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.concurrent.LinkedBlockingQueue

interface Information {
    /** Who or what sent the information, or where does it come from. */
    val origin: Origin

    /** Session information about the information. */
    val session: Session

    /** When exactly the information was created. */
    val createdAt: ZonedDateTime

    /** Whether the information is urgent. */
    val urgent: Boolean

    /** How long the information is valid, if at all. */
    val expires: Expiration

    /** The state of the information. */
    val state: State

    /** A list of states (in order) the information went through. */
    val states: List<State>

    /** Whether the information is new and was never enqueued nor dispatched. */
    val new: Boolean

    /** Whether the information was ever enqueued. */
    val enqueued: Boolean

    /** Whether the information was ever dispatched. */
    val dispatched: Boolean

    /** Number of ovservers attached to the information. */
    val observerCount: Int

    /** A fresh copy of this information. */
    val copy: Information

    /** Registers the specified observer for state changes. @return `true` if the observer was enregistered. */
    fun enregister(observer: Observer): Boolean

    /** Removes the specified observer from the state change registry. @return `true` if the observer was deregistered. */
    fun deregister(observer: Observer): Boolean

    /**
     * Marks the information as enqueued.
     * @return the same information for chaining.
     */
    fun enqueue(): Information

    /**
     * Marks the information as dispatched.
     * @return the same information for chaining.
     */
    fun dispatch(): Information

    /**
     * Marks the information as delivered.
     * @return the same information for chaining.
     */
    fun delivered(): Information

    /**
     * Marks the information as processed.
     * @return the same information for chaining.
     */
    fun processed(): Information

    /**
     * Marks the information as lost.
     * @return the same information for chaining.
     */
    fun miss(): Information

    /** Triggers the specified action whenever the specified state is reached. */
    fun whenState(state: State, action: () -> Unit)

    /** Wraps this information into a message to the specified recipient. */
    fun withDefaultRecipient(target: Recipient?): Message
    /** Wraps this information into a message to the specified recipient. */
    fun withRecipient(target: Recipient): Message
    override fun toString(): String

    companion object {
        val LOST_ITEMS = LinkedBlockingQueue<Information>()
    }
}
