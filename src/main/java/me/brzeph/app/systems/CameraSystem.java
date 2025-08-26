package me.brzeph.app.systems;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class CameraSystem extends BaseAppState {
    private final Node playerNode;
    private final Camera cam;

    public CameraSystem(Node playerNode, Camera cam) {
        this.playerNode = playerNode;
        this.cam = cam;
    }

    @Override
    protected void initialize(Application app) {
        ((SimpleApplication) app).getFlyByCamera().setEnabled(false);
    }

    @Override
    public void update(float tpf) {
        Vector3f pos = playerNode.getWorldTranslation().add(0, 2, 8);
        cam.setLocation(pos);
        cam.lookAt(playerNode.getWorldTranslation().add(0, 1.5f, 0), Vector3f.UNIT_Y);
    }

    @Override protected void cleanup(Application app) {}
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}

