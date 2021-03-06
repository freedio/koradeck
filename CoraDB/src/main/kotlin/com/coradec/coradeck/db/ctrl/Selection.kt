package com.coradec.coradeck.db.ctrl

import com.coradec.coradeck.core.util.trimIfBlank

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
