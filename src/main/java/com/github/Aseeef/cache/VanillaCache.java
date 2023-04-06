package com.github.Aseeef.cache;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class VanillaCache<K, V> implements AseefianCache<K,V> {

    private final Map<K, V> cache;

    public VanillaCache(int size) {
        cache = Collections.synchronizedMap(new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > size; //cache size
            }
        });
    }

    @Override
    public void put(@NonNull K key, @Nullable V value) {
        cache.put(key, value);
    }

    @Override
    public @Nullable V getIfPresent(K key) {
        return cache.getOrDefault(key, null);
    }
}
