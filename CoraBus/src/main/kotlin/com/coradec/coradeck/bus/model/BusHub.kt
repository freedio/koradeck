/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.Voucher

interface BusHub : BusNode {
    /** A list of all visible members by name. */
    val members: Voucher<Map<String, BusNode>>
    /** A list of the names of all visible members. */
    val names: Voucher<Set<String>>
    /** Retrieves the member with the specified name, if available. */
    fun lookup(name: String): Voucher<BusNode>
    /** Adds the specified node as a member to this hub under the specified name and returns a request for it. */
    fun add(name: String, node: BusNode): Request
    /** Removes the member with the specified name and returns a voucher for it. */
    fun remove(name: String): Voucher<BusNode>
    /** Replaces the member with the specified name by the specified sustitute and returns a voucher for the previous node. */
    fun replace(name: String, substitute: BusNode): Voucher<BusNode>
    /** Renames the member with the specified name to the specified new name. */
    fun rename(name: String, newName: String): Request
}
