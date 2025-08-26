package me.brzeph.infra.jme.adapter;

import com.jme3.scene.Spatial;

import java.util.concurrent.ConcurrentHashMap;

public class SceneRegistry {
    private static final ConcurrentHashMap<Long, Spatial> REG = new ConcurrentHashMap<>();
    private SceneRegistry(){}
    public static void put(long id, Spatial s) { REG.put(id, s); }
    public static Spatial get(long id) { return REG.get(id); }
    public static void remove(long id) { REG.remove(id); }
}
