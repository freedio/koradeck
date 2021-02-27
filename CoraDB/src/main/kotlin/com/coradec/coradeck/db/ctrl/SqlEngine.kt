package com.coradec.coradeck.db.ctrl

import com.coradec.coradeck.db.ctrl.impl.BasicSqlEngine
import java.util.stream.Stream
import kotlin.reflect.KClass

interface SqlEngine<T: Any> {
    val tableName: String

    /** Counts the rows in the table limited by the specified query. */
    fun count(selector: Selection): Int
    /** Queries the data in the table limited by the specified query. */
    fun query(selector: Selection): Stream<T>
    /** Asserts that the table exists. */
    fun assertTable()
    /** Inserts the specified record to the table; returns the number of affected rows (should be 1). */
    fun insert(entry: T): Int
    /** Deletes the records selected by the specified selection; returns the number of affected rows. */
    fun delete(selector: Selection): Int
    /** Commit all uncommitted changes so far. */
    fun commit()

    companion object {
        operator fun <T: Any> invoke(klass: KClass<out T>): SqlEngine<T> = BasicSqlEngine(klass)
    }
}
