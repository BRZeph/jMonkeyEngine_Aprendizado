package me.brzeph.infra.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> handlers = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> handler) {
        handlers.computeIfAbsent(type, k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        var list = handlers.getOrDefault(event.getClass(), List.of());
        for (Consumer<?> c : list) ((Consumer<T>) c).accept(event);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EventBus{\n");
        handlers.forEach((key, list) -> {
            sb.append("  Event type: ").append(key.getSimpleName())
                    .append(", handlers count: ").append(list.size()).append("\n");
        });
        sb.append("}");
        return sb.toString();
    }

}