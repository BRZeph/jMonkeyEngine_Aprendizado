package me.brzeph.core.domain.entity.enemies.behaviour;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.*;

public class GameStateContext {

    private static GameStateContext context;

    private final Map<Class<?>, Object> REGISTRY = new ConcurrentHashMap<>();

    // Armazena uma única instância
    public <T> void put(Class<T> type, T instance) {
        REGISTRY.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) REGISTRY.get(type);
    }

    public <T> void remove(Class<T> type) {
        REGISTRY.remove(type);
    }

    // Armazena uma lista de objetos do tipo T
    public <T> void putList(Class<T> type, List<T> list) {
        REGISTRY.put(type, list);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(Class<T> type) {
        return (List<T>) REGISTRY.getOrDefault(type, Collections.emptyList());
    }

    public static GameStateContext get() {
        if (context == null) {
            context = new GameStateContext();
        }
        return context;
    }
}


