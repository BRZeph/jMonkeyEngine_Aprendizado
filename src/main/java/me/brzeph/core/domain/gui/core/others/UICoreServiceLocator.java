package me.brzeph.core.domain.gui.core.others;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UICoreServiceLocator {
    private static final Map<Class<?>, Object> REGISTRY = new ConcurrentHashMap<>();
    public static <T> void bind(Class<T> type, T instance){ REGISTRY.put(type, instance); }
    public static <T> T get(Class<T> type){ return type.cast(REGISTRY.get(type)); }
}
