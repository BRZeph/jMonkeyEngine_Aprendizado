package me.brzeph.app.systems.impl.collisionSystem.helpers;

import me.brzeph.core.domain.entity.GameEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CollisionProfiles {

    private final Map<Class<?>, CollisionProfile> byClass = new HashMap<>();
    private final Map<Class<?>, CollisionProfile> cache   = new ConcurrentHashMap<>();
    private final Map<String, CollisionProfile>   byId    = new ConcurrentHashMap<>();

    /** Associa um perfil a uma classe (ou interface) de GameEntity */
    public CollisionProfiles map(Class<? extends GameEntity> cls, CollisionProfile p){
        byClass.put(cls, p);
        cache.clear();
        return this;
    }

    /** Override por instância específica (ex.: um item que também colide com MONSTER) */
    public void override(GameEntity e, CollisionProfile p){
        byId.put(e.getId(), p);
    }
    public void clearOverride(GameEntity e){
        byId.remove(e.getId());
    }

    /** Perfil efetivo para uma entidade (override > classe > fallback) */
    public CollisionProfile forEntity(GameEntity e){
        CollisionProfile ov = byId.get(e.getId());
        return (ov != null) ? ov : forClass(e.getClass());
    }

    /** Perfil para uma classe (com cache e fallback por herança/interfaces) */
    public CollisionProfile forClass(Class<?> cls){
        return cache.computeIfAbsent(cls, this::searchHierarchy);
    }

    /** Grupo (bit) rápido da entidade (útil na InteractionMatrix) */
    public int groupOf(GameEntity e){
        return forEntity(e).group();
    }

    private CollisionProfile searchHierarchy(Class<?> c){
        // 1) Classe subindo na hierarquia
        Class<?> k = c;
        while (k != null) {
            CollisionProfile p = byClass.get(k);
            if (p != null) return p;
            // 2) Interfaces desta classe
            for (Class<?> it : k.getInterfaces()) {
                p = byClass.get(it);
                if (p != null) return p;
            }
            k = k.getSuperclass();
        }
        // 3) Fallback (tudo ≈ WORLD)
        return new CollisionProfile(CollLayers.WORLD, CollLayers.ALL);
    }
}
