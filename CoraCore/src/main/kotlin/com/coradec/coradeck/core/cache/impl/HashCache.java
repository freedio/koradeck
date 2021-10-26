/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.cache.impl;

import java.util.Objects;

/**
 * ​​Implementation of a cache based on a hash map.
 */
public class HashCache<K, V> extends BasicCache<K, V> {

    /**
     * Initializes a new instance of HashCache with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor      the load factor.
     */
    public HashCache(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Initializes a new instance of HashCache with the specified initial capacity and default load
     * factor.
     *
     * @param initialCapacity the initial capacity.
     */
    public HashCache(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Initializes a new instance of HashCache with default initial capacity and default load
     * factor.
     */
    public HashCache() {
    }

    /**
     * Compares two keys with each other for equality.
     *
     * @param key1 one key.
     * @param key2 the other key.
     * @return {@code true} if both keys are equal, {@code false} if the are different.
     */
    @Override
    protected boolean isEqual(final Object key1, final K key2) {
        return Objects.equals(key1, key2);
    }
}
