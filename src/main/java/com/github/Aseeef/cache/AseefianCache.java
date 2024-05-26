package com.github.Aseeef.cache;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AseefianCache<K,V> {

    public void put(@NonNull K key, @Nullable V value);

    public @Nullable V getIfPresent(K key);

    public default @NonNull V getOrElse(K key, V defaultValue) {
        V retVal = getIfPresent(key);
        if (retVal == null)
            retVal = defaultValue;
        return retVal;
    };

}
