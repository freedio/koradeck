package com.coradec.coradeck.db.util

import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.util.formatted
import com.coradec.module.db.trouble.ExcessResultsException
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.function.Consumer
import kotlin.NoSuchElementException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

@Suppress("UNCHECKED_CAST")
fun <T : Any> ResultSet.encode(klass: KClass<T>): T {
    if (klass == Int::class) return getInt(1) as T
    if (klass == String::class) return getString(1) as T
    val sqlValues: Map<String, Any?> = klass.fields
            .mapKeys { (name, _) -> name.toSqlObjectName() }
            .map { Pair(it.value.name, getObjectOrNull(it.key).toSqlFieldValue()) }.toMap()
    val pcon = klass.primaryConstructor ?: throw IllegalArgumentException("$klass has no primary constructor!")
    val args = pcon.valueParameters.map { Pair(it, it.name) }.toMap().mapValues { sqlValues[it.value] }
    CoraCom.log.debug("Calling ${pcon.name}(${args.entries.joinToString { "${it.key.name}:${it.key.type} = ${it.value.formatted}:${it.value?.javaClass?.name}" }})")
    return pcon.callBy(args)
}

fun ResultSet.getObjectOrNull(name: String): Any? = try {
    getObject(name)
} catch (e: SQLException) {
    if (e.message?.contains("Column not found: ") == true) null else throw e
}

fun <T : Any> ResultSet.singleOf(klass: KClass<T>): T {
    if (!next()) throw NoSuchElementException("ResultSet has no rows (left)")
    return encode(klass)
}

fun <T : Any> ResultSet.checkedSingleOf(klass: KClass<T>): T {
    if (!next()) throw NoSuchElementException("ResultSet has no rows (left)")
    val result = encode(klass)
    if (next()) throw ExcessResultsException("ResultSet has more than 1 row (left)")
    return result
}

fun <T : Any> ResultSet.listOf(klass: KClass<T>): List<T> {
    val result: MutableList<T> = mutableListOf<T>()
    while (next()) {
        result += encode(klass)
    }
    return result.toList()
}

fun ResultSet.forEach(action: (ResultSet) -> Unit) {
    while (next()) action.invoke(this)
}

fun <T> ResultSet.map(transform: (ResultSet) -> T): Sequence<T> = Sequence {
    object : Iterator<T> {
        override fun hasNext(): Boolean = this@map.next()
        override fun next(): T = transform(this@map)
    }
}

operator fun ResultSet.get(columnName: String): Any = getObject(columnName)

class ResultSetSpliterator<T : Any>(private val dataset: ResultSet, private val klass: KClass<T>) : Spliterator<T> {
    override fun tryAdvance(action: Consumer<in T>): Boolean = dataset.next().also { if (it) action.accept(dataset.encode(klass)) }
    override fun trySplit(): Spliterator<T>? = null
    override fun estimateSize(): Long = Long.MAX_VALUE
    override fun characteristics(): Int = Spliterator.ORDERED or Spliterator.IMMUTABLE
}
