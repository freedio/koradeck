/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.cache.impl;

import com.coradec.coradeck.core.cache.Cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Math.round;

/**
 * ​​Basic implementation of a cache.  A cache is a map whose entry are remove by the garbage collector upon memory saturation
 * when the key is no longer referenced.
 * <p>
 * Basic cache implements all optional map operation.  It is not thread safe.  For a thread safe implementations, see
 * ConcurrentCache.
 */
@SuppressWarnings({
                          "LocalVariableOfConcreteClass", "CastToConcreteClass", "MethodCallInLoopCondition",
                          "InstanceVariableOfConcreteClass", "MethodReturnOfConcreteClass", "ForLoopWithMissingComponent",
                          "MethodWithMultipleLoops", "MethodParameterOfConcreteClass", "ReturnOfNull", "PackageVisibleField",
                          "NewExceptionWithoutArguments", "NonStaticInnerClassInSecureContext", "ClassWithTooManyMethods",
                          "OverlyComplexClass", "ClassWithoutLogger"
                  })
abstract class BasicCache<K, V> implements Cache<K, V> {

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    int modCount;
    private Set<K> keySet;
    private Collection<V> values;
    private Set<Entry<K, V>> entrySet;

    private static Entry<Integer, String> createEntry(final int i, final String s) {
        return null;
    }

    @SuppressWarnings({"PackageVisibleField", "InstanceVariableOfConcreteClass"})
    static class CacheEntry<K, V> extends SoftReference<K> implements Entry<K, V> {

        CacheEntry<K, V> next;
        V value;
        final int hash;

        /**
         * Initializes a new instance of CacheEntry.
         *
         * @param key   the key.
         * @param queue the queue with which the reference is to be registered.
         * @param hash  the has value.
         * @param next  the next entry.
         */
        CacheEntry(final K key, final V value, final ReferenceQueue<? super K> queue, final int hash,
                final CacheEntry<K, V> next) {
            super(key, queue);
            this.hash = hash;
            this.value = value;
            this.next = next;
        }

        @Override public K getKey() {
            return get();
        }

        @Override public V getValue() {
            return this.value;
        }

        @Override public V setValue(final V value) {
            final V previous = this.value;
            this.value = value;
            return previous;
        }

        @Override public boolean equals(final Object obj) {
            boolean result = false;
            if (obj instanceof Map.Entry<?, ?>) {
                final Entry<?, ?> e = (Entry<?, ?>)obj;
                final Object k1 = getKey();
                final Object k2 = e.getKey();
                if (Objects.equals(k1, k2)) {
                    final Object v1 = getValue();
                    final Object v2 = e.getValue();
                    result = Objects.equals(v1, v2);
                }
            }
            return result;
        }

        @Override public int hashCode() {
            final K key = getKey();
            final Object val = getValue();
            return (key == null ? 0 : key.hashCode()) ^ (val == null ? 0 : val.hashCode());
        }

        @Override public String toString() {
            return getKey() + "=" + getValue();
        }

    }

    int size;
    private int threshold;
    private final float loadFactor;

    /** The table, resized as necessary. Length MUST always be a power of two. */
    CacheEntry<K, V>[] table;

    /** Reference queue for cleared WeakEntries */
    private final ReferenceQueue<K> queue = new ReferenceQueue<>();

    /**
     * Applies a supplemental hash function to a given hashCode, which defends against poor quality hash functions. This is
     * critical because HashMap uses power-of-two length hash tables, that otherwise encounter collisions for hashCodes that do
     * not differ in lower bits.
     *
     * @param hv the hash code to transform.
     * @return a modified hash code.
     */
    @SuppressWarnings("MagicNumber") private static int rehash(final int hv) {
        int h = hv;
        h += ~(h << 9);
        h ^= h >>> 14;
        h += h << 4;
        h ^= h >>> 10;
        return h;
    }

    /**
     * Returns the key's hash code, corrected.
     *
     * @param key the key whose hash code to retrieve (optional).
     * @return the key's hash code, corrected.
     */
    protected int hash(final Object key) {
        return rehash(key == null ? 0 : key.hashCode());
    }

    /**
     * Returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no mapping for
     * this key.
     *
     * @param key the key.
     * @return the associated entry.
     */
    private CacheEntry<K, V> getEntry(final Object key) {
        final int h = hash(key);
        final CacheEntry<K, V>[] tab = getTable();
        final int index = indexFor(h, tab.length);
        CacheEntry<K, V> e = tab[index];
        while (e != null && !(e.hash == h && isEqual(key, e.get()))) {
            e = e.next;
        }
        //noinspection ConstantConditions
        return e;
    }

    /**
     * Compares two keys with each other for equality.
     *
     * @param key1 one key.
     * @param key2 the other key.
     * @return {@code true} if both keys are equal, {@code false} if the are different.
     */
    protected abstract boolean isEqual(final Object key1, final K key2);

    /**
     * Special-case code for containsValue with null argument
     *
     * @return {@code true} if {@code null} is contained.
     */
    private boolean containsNullValue() {
        boolean result = false;
        final CacheEntry<K, V>[] tab = getTable();
        for (int i = tab.length; i-- > 0; ) {
            for (CacheEntry<K, V> e = tab[i]; e != null; e = e.next) {
                if (e.value == null) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Rehashes the contents of this map into a new array with a larger capacity. This method is called automatically when the
     * number of keys in this map reaches its threshold.
     * <p>
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two; must be greater than current capacity unless current
     *                    capacity is MAXIMUM_CAPACITY (in which case value is irrelevant).
     */
    private void resize(final int newCapacity) {
        final CacheEntry<K, V>[] oldTable = getTable();
        final int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }

        //noinspection unchecked
        final CacheEntry<K, V>[] newTable = new CacheEntry[newCapacity];
        transfer(oldTable, newTable);
        this.table = newTable;

        /*
         * If ignoring null elements and processing ref queue caused massive
         * shrinkage, then restore old table. This should be rare, but avoids
         * unbounded expansion of garbage-filled tables.
         */
        if (this.size >= this.threshold / 2) {
            this.threshold = (int)(newCapacity * this.loadFactor);
        } else {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            this.table = oldTable;
        }
    }

    /**
     * Transfers all entries from src to dst tables.
     *
     * @param src source of transfer.
     * @param dst destination of transfer.
     */
    @SuppressWarnings({
                              "AssignmentToNull", "MethodCanBeVariableArityMethod", "FieldRepeatedlyAccessedInMethod"
                      }) private void transfer(final CacheEntry<K, V>[] src, final CacheEntry<K, V>[] dst) {
        for (int i = 0, is = src.length; i < is; ++i) {
            CacheEntry<K, V> e = src[i];
            src[i] = null;
            while (e != null) {
                final CacheEntry<K, V> next = e.next;
                final Object key = e.get();
                //noinspection VariableNotUsedInsideIf
                if (key == null) this.size--;
                else {
                    final int ix = indexFor(e.hash, dst.length);
                    e.next = dst[ix];
                    dst[ix] = e;
                }
                e = next;
            }
        }
    }

    /**
     * Expunge stale entries from the table.
     */
    private void expungeStaleEntries() {
        CacheEntry<? extends K, ? extends V> e;
        //noinspection unchecked
        int size = this.size;
        final CacheEntry<K, V>[] table = this.table;
        while ((e = (CacheEntry<? extends K, ? extends V>)this.queue.poll()) != null) {
            final int h = e.hash;
            final int i = indexFor(h, table.length);

            CacheEntry<K, V> prev = table[i];
            CacheEntry<K, V> p = prev;
            while (p != null) {
                final CacheEntry<K, V> next = p.next;
                if (p == e) {
                    if (prev == e) {
                        table[i] = next;
                    } else {
                        prev.next = next;
                    }
                    --size;
                    break;
                }
                prev = p;
                p = next;
            }
        }
        this.size = size;
    }

    /**
     * Returns the index for the specified hash code.
     *
     * @param h      the hash code.
     * @param length the table length.
     * @return the index for the hash code.
     */
    private static int indexFor(final int h, final int length) {
        return h & length - 1;
    }

    /**
     * Checks for equality of non-null reference x and possibly-null y.
     *
     * @param x either object.
     * @param y either object.
     * @return {@code true} if both objects are to be considered equal.
     */
    @SuppressWarnings("ObjectEquality") protected boolean eq(final Object x, final Object y) {
        return x == y || x.equals(y);
    }

    /**
     * Return the table after first expunging stale entries.
     *
     * @return the table after first expunging stale entries.
     */
    private CacheEntry<K, V>[] getTable() {
        expungeStaleEntries();
        return this.table;
    }

    // Constructors
    // ============

    /**
     * Initializes a new instance of BasicCache with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor      the load factor.
     */
    public BasicCache(final int initialCapacity, final float loadFactor) {
        int initCap = initialCapacity;
        if (initCap > MAXIMUM_CAPACITY) {
            initCap = MAXIMUM_CAPACITY;
        }
        int capacity = 1;
        while (capacity < initCap) {
            capacity <<= 1;
        }
        //noinspection unchecked
        this.table = new CacheEntry[capacity];
        this.loadFactor = loadFactor;
        this.threshold = round(capacity * loadFactor);
    }

    /**
     * Initializes a new instance of BasicCache with the specified initial capacity and default load factor.
     *
     * @param initialCapacity the initial capacity.
     */
    public BasicCache(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Initializes a new instance of BasicCache with default initial capacity and default load factor.
     */
    public BasicCache() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    // Business methods
    // ================

    @Override public int size() {
        if (this.size != 0) expungeStaleEntries();
        return this.size;
    }

    @Override public boolean isEmpty() {
        return this.size == 0;
    }

    @Override public boolean containsKey(final Object key) {
        return getEntry(key) != null;
    }

    @Override public boolean containsValue(final Object value) {
        boolean result = false;
        if (value == null) result = containsNullValue();
        else {
            final CacheEntry<K, V>[] tab = getTable();
            for (int i = tab.length; i-- > 0; ) {
                for (CacheEntry<K, V> e = tab[i]; e != null; e = e.next) {
                    if (value.equals(e.value)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("ReturnOfNull") @Override public V get(final Object key) {
        V result = null;
        if (key != null) {
            final int h = hash(key);
            final CacheEntry<K, V>[] tab = getTable();
            final int index = indexFor(h, tab.length);
            CacheEntry<K, V> e = tab[index];
            while (e != null) {
                if (e.hash == h && isEqual(key, e.get())) {
                    result = e.value;
                    break;
                }
                e = e.next;
            }
        }
        return result;
    }

    @SuppressWarnings({"ObjectEquality", "MethodWithMultipleReturnPoints"}) @Override public V put(final K key, final V value) {
        final int h = hash(key);
        final CacheEntry<K, V>[] tab = getTable();
        final int i = indexFor(h, tab.length);

        for (CacheEntry<K, V> e = tab[i]; e != null; e = e.next) {
            if (h == e.hash && isEqual(key, e.get())) {
                final V oldValue = e.value;
                if (value != oldValue) {
                    e.value = value;
                }
                return oldValue;
            }
        }

        this.modCount++;
        final CacheEntry<K, V> e = tab[i];
        tab[i] = new CacheEntry<>(key, value, this.queue, h, e);
        if (++this.size >= this.threshold) {
            resize(tab.length << 1);
        }
        return null;
    }

    @SuppressWarnings({
                              "ObjectEquality", "FieldRepeatedlyAccessedInMethod", "MethodWithMultipleReturnPoints"
                      }) @Override public V remove(final Object key) {
        final int h = hash(key);
        final CacheEntry<K, V>[] tab = getTable();
        final int i = indexFor(h, tab.length);
        CacheEntry<K, V> prev = tab[i];
        CacheEntry<K, V> e = prev;

        while (e != null) {
            final CacheEntry<K, V> next = e.next;
            if (h == e.hash && isEqual(key, e.get())) {
                this.modCount++;
                this.size--;
                if (prev == e) {
                    tab[i] = next;
                } else {
                    prev.next = next;
                }
                return e.value;
            }
            prev = e;
            e = next;
        }

        return null;
    }

    @Override public void putAll(final Map<? extends K, ? extends V> input) {
        final int numKeysToBeAdded = input.size();
        if (numKeysToBeAdded == 0) {
            return;
        }

        /*
         * Expand the map if the number of mappings to be added is greater than or equal to
         * threshold. This is conservative; the obvious condition is (m.size() + size) ≥
         * threshold, but this condition could result in a map with twice the appropriate
         * capacity, if the keys to be added overlap with the keys already in this map. By using
         * the conservative calculation, we subject ourselves to at most one extra resize.
         */
        if (numKeysToBeAdded > this.threshold) {
            int targetCapacity = round(numKeysToBeAdded / this.loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY) {
                targetCapacity = MAXIMUM_CAPACITY;
            }
            int newCapacity = getTable().length;
            while (newCapacity < targetCapacity) {
                newCapacity <<= 1;
            }
            if (newCapacity > getTable().length) {
                resize(newCapacity);
            }
        }

        for (final Entry<? extends K, ? extends V> e : input.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("StatementWithEmptyBody") @Override public void clear() {
        // clear out ref queue. We don't need to expunge entries since table is getting cleared.
        while (this.queue.poll() != null) ;

        this.modCount++;
        final CacheEntry<K, V>[] tab = getTable();
        for (int i = 0, is = tab.length; i < is; ++i) {
            //noinspection AssignmentToNull
            tab[i] = null;
        }
        this.size = 0;

        // Allocation of array may have caused GC, which may have caused
        // additional entries to go stale. Removing these entries from the
        // reference queue will make them eligible for reclamation.
        while (this.queue.poll() != null) ;
    }

    @Override public Set<K> keySet() {
        final Set<K> ks = this.keySet;
        return ks != null ? ks : (this.keySet = new KeySet());
    }

    @Override public Collection<V> values() {
        final Collection<V> vs = this.values;
        return vs != null ? vs : (this.values = new Values());
    }

    @Override public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> es = this.entrySet;
        return es != null ? es : (this.entrySet = new EntrySet());
    }

    /**
     * Representation of a set of the keys in the cache.
     */
    private class KeySet implements Set<K> {

        KeySet() {
        }

        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection
         */
        @Override public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override public int size() {
            return BasicCache.this.size;
        }

        /**
         * Returns <tt>true</tt> if this set contains no elements.
         *
         * @return <tt>true</tt> if this set contains no elements
         */
        @Override public boolean isEmpty() {
            return false;
        }

        @Override public boolean contains(final Object o) {
            return containsKey(o);
        }

        @Override public boolean remove(final Object o) {
            final boolean result;
            //noinspection AssignmentUsedAsCondition
            if (result = containsKey(o)) {
                BasicCache.this.remove(o);
            }
            return result;
        }

        @Override public boolean containsAll(final Collection<?> coll) {
            boolean result = true;
            for (Object o : coll) {
                if (!containsKey(o)) {
                    result = false;
                    break;
                }
            }
            return result;
        }

        @Override public boolean addAll(final Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean retainAll(final Collection<?> coll) {
            boolean result = false;
            for (final Iterator<K> it = iterator(); it.hasNext(); )
                if (!coll.contains(it.next())) {
                    it.remove();
                    result = true;
                }
            return result;
        }

        @Override public boolean removeAll(final Collection<?> coll) {
            boolean result = false;
            for (Object o : coll) {
                result |= BasicCache.this.remove(o) != null;
            }
            return result;
        }

        @Override public void clear() {
            BasicCache.this.clear();
        }

        @Override public Object[] toArray() {
            final Object[] result = new Object[size()];
            int i = 0;
            for (final K key : keySet()) {
                result[i++] = key;
            }
            return result;
        }

        @SuppressWarnings("AssignmentToMethodParameter") @Override public <T> T[] toArray(T[] a) {
            if (a.length < BasicCache.this.size) {
                a = (T[])Array.newInstance(a.getClass().getComponentType(), BasicCache.this.size);
            }
            int i = 0;
            for (final K key : keySet()) a[i++] = (T)key;
            return a;
        }

        @Override public boolean add(final K key) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Representation of the collection of values in the cache.
     */
    private class Values implements Collection<V> {

        Values() {
        }

        @Override public int size() {
            return BasicCache.this.size();
        }

        /**
         * Returns <tt>true</tt> if this collection contains no elements.
         *
         * @return <tt>true</tt> if this collection contains no elements
         */
        @Override public boolean isEmpty() {
            return BasicCache.this.isEmpty();
        }

        @Override public boolean contains(final Object o) {
            return BasicCache.this.containsValue(o);
        }

        @Override public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override public Object[] toArray() {
            final Object[] result = new Object[size()];
            int i = 0;
            for (final V val : values()) {
                result[i++] = val;
            }
            return result;
        }

        @SuppressWarnings("AssignmentToMethodParameter") @Override public <T> T[] toArray(T[] a) {
            if (a.length < BasicCache.this.size) {
                a = (T[])Array.newInstance(a.getClass().getComponentType(), BasicCache.this.size);
            }
            int i = 0;
            for (final V val : values()) a[i++] = (T)val;
            return a;
        }

        @Override public boolean add(final V v) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean remove(final Object o) {
            boolean result = false;
            for (final Iterator<Entry<K, V>> it = entrySet().iterator(); it.hasNext(); ) {
                if (Objects.equals(o, it.next().getValue())) {
                    it.remove();
                    result = true;
                }
            }
            return result;
        }

        @Override public boolean containsAll(final Collection<?> coll) {
            boolean result = true;
            for (final Object o : coll) {
                if (!BasicCache.this.containsValue(o)) {
                    result = false;
                    break;
                }
            }
            return result;
        }

        @Override public boolean addAll(final Collection<? extends V> coll) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean removeAll(final Collection<?> coll) {
            boolean result = false;
            for (final Iterator<?> it = coll.iterator(); it.hasNext(); ) {
                if (contains(it.next())) {
                    it.remove();
                    result = true;
                }
            }
            return result;
        }

        @Override public boolean retainAll(final Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        @Override public void clear() {
            BasicCache.this.clear();
        }
    }

    /**
     * Implementation of an entry set for the cache.
     */
    private class EntrySet implements Set<Entry<K, V>> {

        @Override public int size() {
            return BasicCache.this.size();
        }

        @Override public boolean isEmpty() {
            return BasicCache.this.isEmpty();
        }

        @Override public boolean contains(final Object o) {
            boolean result = false;
            if (o instanceof Map.Entry) {
                for (final Entry<K, V> entry : this) {
                    if (isEqual(((Entry<K, V>)o).getKey(), entry.getKey()) &&
                        Objects.equals(((Entry<K, V>)o).getValue(), entry.getValue())) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        }

        @Override public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override public Object[] toArray() {
            final Object[] result = new Object[size()];
            int i = 0;
            for (final V val : values()) {
                result[i++] = val;
            }
            return result;
        }

        @SuppressWarnings("AssignmentToMethodParameter") @Override public <T> T[] toArray(T[] a) {
            if (a.length < BasicCache.this.size) {
                a = (T[])Array.newInstance(a.getClass().getComponentType(), BasicCache.this.size);
            }
            int i = 0;
            for (final Entry<K, V> entry : entrySet()) a[i++] = (T)entry;
            return a;
        }

        @Override public boolean add(final Entry<K, V> kvEntry) {
            return put(kvEntry.getKey(), kvEntry.getValue()) == null;
        }

        @Override public boolean remove(final Object o) {
            return o instanceof Map.Entry && BasicCache.this.remove(((Entry<K, V>)o).getKey()) != null;
        }

        @Override public boolean containsAll(final Collection<?> coll) {
            boolean result = true;
            for (Object o : coll) {
                if (!(o instanceof Map.Entry) || !get(((Entry<K, V>)o).getKey()).equals(((Entry<K, V>)o).getValue())) {
                    result = false;
                    break;
                }
            }
            return result;
        }

        @Override public boolean addAll(final Collection<? extends Entry<K, V>> coll) {
            boolean result = false;
            for (Entry<K, V> entry : coll) {
                result |= put(entry.getKey(), entry.getValue()) == null;
            }
            return result;
        }

        @Override public boolean retainAll(final Collection<?> coll) {
            boolean result = false;
            for (final Iterator<Entry<K, V>> it = iterator(); it.hasNext(); ) {
                if (!coll.contains(it.next())) {
                    it.remove();
                    result = true;
                }
            }
            return result;
        }

        @Override public boolean removeAll(final Collection<?> coll) {
            boolean result = false;
            for (Object o : coll) {
                if (o instanceof Map.Entry && get(((Entry<K, V>)o).getKey()).equals(((Entry<K, V>)o).getValue())) {
                    result |= remove(o);
                }
            }
            return result;
        }

        @Override public void clear() {
            BasicCache.this.clear();
        }
    }

    private class KeyIterator extends CacheIterator<K> {

        @Override protected K partOf(final CacheEntry<K, V> entry) {
            return entry.getKey();
        }
    }

    private class ValueIterator extends CacheIterator<V> {

        @Override protected Object partOf(final CacheEntry<K, V> entry) {
            return entry.getValue();
        }

    }

    private class EntryIterator extends CacheIterator<Entry<K, V>> {

        @Override protected Object partOf(final CacheEntry<K, V> entry) {
            return entry;
        }
    }

    /**
     * Base class of all cache iterators.
     *
     * @param <T> the iterated type.
     */
    @SuppressWarnings({
                              "NewExceptionWithoutArguments", "AssignmentToNull", "ClassNamePrefixedWithPackageName"
                      })
    private abstract class CacheIterator<T> implements Iterator<T> {

        /** Running index. */
        private int index;
        /** Current entry. */
        private CacheEntry<K, V> entry;
        /** Reference to entry last returned. */
        private CacheEntry<K, V> lastReturned;
        /** The mod count of the underlying cache. */
        private int expectedModCount = BasicCache.this.modCount;
        /** Strong reference needed to avoid disappearance of key between hasNext and next. */
        private K nextKey;
        /** Strong ref to avoid disappearance of key between nextEntry() and any use of entry. */
        private K currentKey;

        CacheIterator() {
            this.index = isEmpty() ? 0 : BasicCache.this.getTable().length;
        }

        @SuppressWarnings("FieldRepeatedlyAccessedInMethod") @Override public boolean hasNext() {
            final CacheEntry<K, V>[] t = BasicCache.this.getTable();
            boolean result = true;
            while (this.nextKey == null) {
                CacheEntry<K, V> e = this.entry;
                int i = this.index;
                while (e == null && i > 0) {
                    e = t[--i];
                }
                this.entry = e;
                this.index = i;
                if (e == null) {
                    this.currentKey = null;
                    result = false;
                    break;
                }
                this.nextKey = e.get(); // hold on to key by assigning to strong ref
                if (this.nextKey == null) {
                    this.entry = this.entry.next;
                }
            }
            return result;
        }

        @Override public T next() {
            if (BasicCache.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (this.nextKey == null && !hasNext()) {
                throw new NoSuchElementException();
            }

            this.lastReturned = this.entry;
            this.entry = this.entry.next;
            this.currentKey = this.nextKey;
            this.nextKey = null;
            return (T)partOf(this.lastReturned);
        }

        @Override public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }
            if (this.expectedModCount != BasicCache.this.modCount) {
                throw new ConcurrentModificationException();
            }

            if (this.currentKey != null) BasicCache.this.remove(this.currentKey);
            this.expectedModCount = BasicCache.this.modCount;
            this.lastReturned = null;
            this.currentKey = null;
        }

        /**
         * Function to return the required part of the entry (key, value or entry itself).
         *
         * @param entry the entry to pick from.
         * @return the requested part.
         */
        protected abstract Object partOf(final CacheEntry<K, V> entry);

    }

}
