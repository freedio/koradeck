/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.cache;

import java.util.Map;

/**
 * ​A map whose entries are reclaimed by the garbage collector when their keys are no longer
 * referenced.  In contrast to WeakHashHap whose entries are reclaimed on each garbage collection,
 * the entries of Cache implementations should only be reclaimed upon memory saturation (before an
 * OutOfMemoryException is thrown).
 * <p>
 * The rationale for cache implementations is to make the entries available as long as possible,
 * without holding on to them when a rally for memory occurs.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public interface Cache<K, V> extends Map<K, V> {

}
