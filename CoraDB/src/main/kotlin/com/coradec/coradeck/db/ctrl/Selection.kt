/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.ctrl

interface Selection {
    /** The limits: OFFSET, LIMIT. */
    val slice: String
    /** The where clause: WHERE. */
    val where: String
    /** The order clause: ORDER BY. */
    val order: String
    /** The entire selection: OFFSET, LIMIT, WHERE, ORDER BY. */
    val select: String get() = "$where$order$slice"
    /** The entire filter specification: LIMIT, OFFSET, WHERE. */
    val filter: String get() = "$where$slice"
}
