/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.util

import com.coradec.coradeck.com.module.CoraCom.log
import com.coradec.coradeck.core.util.formatted
import com.coradec.module.db.trouble.ExcessResultsException
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

@Suppress("UNCHECKED_CAST")
fun <Record : Any> ResultSet.encode(model: KClass<Record>): Record = when (model) {
    Int::class -> getInt(1) as Record
    String::class -> getString(1) as Record
    else -> {
        val sqlValues: Map<String, Any?> = model.fields
            .mapKeys { (name, _) -> name.toSqlObjectName() }
            .map { Pair(it.value.name, getObjectOrNull(it.key).toSqlFieldValue()) }.toMap()
        val pcon = model.primaryConstructor ?: throw IllegalArgumentException("$model has no primary constructor!")
        val args = pcon.valueParameters.associateWith { it.name }.mapValues { sqlValues[it.value] }
        log.debug("Calling ${pcon.name}(${args.entries
            .joinToString { "${it.key.name}:${it.key.type} = ${it.value.formatted}:${it.value?.javaClass?.name}" }})")
        pcon.callBy(args)
    }
}

fun ResultSet.getObjectOrNull(name: String): Any? = try {
    getObject(name)
} catch (e: SQLException) {
    if (e.message?.contains("Column not found: ") == true) null else throw e
}

fun <Record : Any> ResultSet.singleOf(model: KClass<Record>): Record {
    if (!next()) throw NoSuchElementException("ResultSet has no rows (left)")
    return encode(model)
}

fun <Record : Any> ResultSet.checkedSingleOf(klass: KClass<Record>): Record {
    if (!next()) throw NoSuchElementException("ResultSet has no rows (left)")
    val result = encode(klass)
    if (next()) throw ExcessResultsException("ResultSet has more than 1 row (left)")
    return result
}

fun <Record : Any> ResultSet.listOf(klass: KClass<Record>): List<Record> {
    val result: MutableList<Record> = mutableListOf<Record>()
    while (next()) {
        result += encode(klass)
    }
    return result.toList()
}

fun <Record : Any> ResultSet.seqOf(model: KClass<Record>): Sequence<Record> = Sequence { ResultSetIterator(this, model) }

fun ResultSet.forEach(action: (ResultSet) -> Unit) {
    while (next()) action.invoke(this)
}

fun <Record : Any> ResultSet.map(transform: (ResultSet) -> Record): Sequence<Record> = Sequence {
    object : Iterator<Record> {
        override fun hasNext(): Boolean = this@map.next()
        override fun next(): Record = transform(this@map)
    }
}

operator fun ResultSet.get(columnName: String): Any = getObject(columnName)

class ResultSetSpliterator<Record : Any>(private val dataset: ResultSet, private val klass: KClass<Record>) : Spliterator<Record> {
    override fun tryAdvance(action: Consumer<in Record>): Boolean = dataset.next().also { if (it) action.accept(dataset.encode(klass)) }
    override fun trySplit(): Spliterator<Record>? = null
    override fun estimateSize(): Long = Long.MAX_VALUE
    override fun characteristics(): Int = Spliterator.ORDERED or Spliterator.IMMUTABLE
}

class ResultSetIterator<Record: Any>(private val dataset: ResultSet, private val model: KClass<Record>): Iterator<Record> {
    private val useLast = AtomicBoolean(false)
    private val last = AtomicBoolean(false)
    private fun useNext(new: Boolean) = if (useLast.getAndSet(new)) last.get() else dataset.next().also { last.set(it) }
    override fun hasNext() = useNext(true)
    override fun next(): Record = if (useNext(false)) dataset.encode(model) else throw NoSuchElementException()
}
