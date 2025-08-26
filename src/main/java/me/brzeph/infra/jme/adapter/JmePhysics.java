package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import me.brzeph.app.ports.Physics;

public class JmePhysics implements Physics {
    private final Application app;
    private BulletAppState bullet;

    public JmePhysics(Application app) {
        this.app = app;
    }

    public void setBullet(BulletAppState bullet) { // chame isso no GameState após criar o BulletAppState
        this.bullet = bullet;
    }

    @Override
    public void addRigidBody(long entityId, float mass) {
        Spatial s = SceneRegistry.get(entityId);
        if (s == null || bullet == null) return;
        RigidBodyControl rbc = s.getControl(RigidBodyControl.class);
        if (rbc == null) {
            rbc = new RigidBodyControl(mass);
            s.addControl(rbc);
        }
        bullet.getPhysicsSpace().add(rbc);
    }

    @Override
    public void applyImpulse(long entityId, Vector3f impulse, Vector3f relPos) {
        Spatial s = SceneRegistry.get(entityId);
        if (s == null) return;
        RigidBodyControl rbc = s.getControl(RigidBodyControl.class);
        if (rbc != null) rbc.applyImpulse(impulse, relPos != null ? relPos : Vector3f.ZERO);
    }

    @Override
    public boolean raycast(Vector3f from, Vector3f to) {
        if (bullet == null) return false;
        // RayTest simples
        var results = bullet.getPhysicsSpace().rayTest(from, to);
        return results != null && !results.isEmpty();
    }
}