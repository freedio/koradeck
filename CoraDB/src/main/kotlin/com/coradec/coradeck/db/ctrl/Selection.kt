package com.coradec.coradeck.db.ctrl

interface Selection {
    /** The entire selection: OFFSET, LIMIT, WHERE, ORDER BY. */
    val select: String
    /** The entire filter specification: LIMIT, OFFSET, WHERE. */
    val filter: String
    /** The limits: OFFSET, LIMIT. */
    val slice: String
    /** The where clause: WHERE. */
    val where: String
    /** The order clause: ORDER BY. */
    val order: String
}
