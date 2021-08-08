package com.coradec.coradeck.db.ctrl.impl

import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.util.toSqlObjectName

open class SqlSelection(private val expr: String) : Selection {
    private val offsetNum: Int by lazy { OFFSET.find(expr)?.groupValues?.let { if (it.size > 2) it[2].toInt() else 0 } ?: 0 }
    private val limitNum: Int by lazy { LIMIT.find(expr)?.groupValues?.let { if (it.size > 2) it[2].toInt() else 0 } ?: 0 }
    private val whereList: List<String> by lazy {
        val resultList = ArrayList<String>()
        WHERE1.findAll(expr).forEach { result -> result.groupValues.let { vals ->
            val op = vals
                .drop(2)
                .dropLast(1)
                .joinToString("") { it.trim().lowercase() }
                .replace(Regex("\\s+"), " ")
            if (vals.size>3) resultList += "${vals[1].toSqlObjectName()} $op ${vals.last()}" }
        }
        WHERE2.findAll(expr).forEach { result -> result.groupValues.let { vals ->
            val op = vals
                .drop(2)
                .joinToString("") { it.trim().lowercase() }
                .replace(Regex("\\s+"), " ")
            if (vals.size>2) resultList += "${vals[1].toSqlObjectName()} $op" }
        }
        resultList.toList()
    }
    private val orderList: List<String> by lazy {
        fun orderExpr(fieldName: String, ordering: String): String = "$fieldName ${ordering.lowercase()}"
        val resultList = ArrayList<String>()
        ORDER.findAll(expr).forEach { result -> result.groupValues.let { if (it.size>2) resultList += orderExpr(it[1], it[2])} }
        resultList.toList()
    }

    val offset: String get() = offsetNum.let { if (it == 0) "" else " offset $it" }
    val limit: String get() = limitNum.let { if (it == 0) "" else " limit $it" }
    override val slice: String get() = "$offset$limit"
    override val where: String get() = whereList.let { map ->
        if (map.isEmpty()) "" else map.joinToString(" and ", " where ") { it }
    }
    override val order: String get() = orderList.let { list ->
        if (list.isEmpty()) "" else list.joinToString(", ", " order by ") { it }
    }

    companion object {
        val ALL = SqlSelection("")
        private val OFFSET = Regex("(OFFSET|Offset|offset):(\\d+)")
        private val LIMIT = Regex("(LIMIT|Limit|limit):(\\d+)")
        private val WHERE1 = Regex("\\[(\\w+)\\s*(=|<=|>=|<>|>|<|" +
                "\\s+IN\\s+|\\s+[Ii]n\\s+|\\s+NOT\\s+IN\\s+|\\s+[Nn]ot\\s+[Ii]n\\s+|" +
                "\\s+LIKE\\s+|\\s+[lL]ike\\s+|\\s+NOT\\s+LIKE\\s+|\\s+[Nn]ot\\s+[Ll]ike\\s+|" +
                "\\s+BETWEEN\\s+|\\s+[Bb]etween\\s+|\\s+NOT\\s+BETWEEN\\s+|\\s+[Nn]ot\\s+[Bb]etween\\s+|" +
                "\\s+IS\\s+DISTINCT\\s+FROM\\s+|\\s+[Ii]s\\s+[Dd]istinct\\s+[Ff]rom\\s+|" +
                "\\s+IS\\s+NOT\\s+DISTINCT\\s+FROM\\s+|\\s+[Ii]s\\s+[Nn]ot\\s+[Dd]istinct\\s+[Ff]rom\\s+)" +
                "\\s*([^]]+)]")
        private val WHERE2 = Regex("\\[(\\w+)\\s+(EXISTS|[Ee]xists|" +
                "IS\\s+NULL|[Ii]s\\s+[Nn]ull|IS\\s+NOT\\s+NULL|[Ii]s\\s+[Nn]ot\\s+[Nn]ull)]")
        private val ORDER = Regex("(\\w+):(ASC|DESC|Asc|Desc|asc|desc)")
    }
}
