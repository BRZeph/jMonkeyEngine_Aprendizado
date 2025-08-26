package me.brzeph.bootstrap;

import java.util.concurrent.ConcurrentHashMap;

public final class ServiceLocator {
    private static final ConcurrentHashMap<Class<?>, Object> REGISTRY = new ConcurrentHashMap<>();
    private ServiceLocator() {}

    public static <T> void put(Class<T> type, T instance) { REGISTRY.put(type, instance); }
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) { return (T) REGISTRY.get(type); }
}