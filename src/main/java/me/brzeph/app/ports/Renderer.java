package me.brzeph.app.ports;

import com.jme3.math.Vector3f;

public interface Renderer {
    void playAnimation(long entityId, String anim, float speed, boolean loop);
    void spawnFx(String assetPath, Vector3f worldPos);
    void setMaterial(long entityId, String matAssetPath);
}
