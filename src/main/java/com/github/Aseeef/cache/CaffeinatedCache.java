package com.github.Aseeef.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CaffeinatedCache<K,V> implements AseefianCache<K,V> {

    private final Cache<K,V> cache;

    public CaffeinatedCache(int size) {
        cache = Caffeine.newBuilder()
                .maximumSize(size)
                .build();
    }

    @Override
    public void put(@NonNull K key, @Nullable V value) {
        cache.put(key, value);
    }

    @Override
    public @Nullable V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }
}
