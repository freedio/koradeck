package com.coradec.coradeck.core.model.impl

class NullIterator<T>: Iterator<T> {
    override fun hasNext(): Boolean = false
    override fun next(): T = throw NoSuchElementException()
}