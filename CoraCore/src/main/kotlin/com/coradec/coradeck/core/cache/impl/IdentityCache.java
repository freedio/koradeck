/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.cache.impl;

/**
 * ​​Implementation of a cache using object identity.
 */
public class IdentityCache<K, V> extends BasicCache<K, V> {

    /**
     * Initializes a new instance of IdentityCache with the specified initial capacity and load
     * factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor      the load factor.
     */
    public IdentityCache(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Initializes a new instance of IdentityCache with the specified initial capacity and default
     * load factor.
     *
     * @param initialCapacity the initial capacity.
     */
    public IdentityCache(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Initializes a new instance of IdentityCache with default initial capacity and default load
     * factor.
     */
    public IdentityCache() {
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    protected boolean isEqual(final Object key1, final K key2) {
        return key1 == key2;
    }

    @Override
    protected int hash(final Object key) {
        return System.identityHashCode(key);
    }
}
