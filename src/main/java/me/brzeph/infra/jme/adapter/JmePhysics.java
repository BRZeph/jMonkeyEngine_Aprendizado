package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.app.ports.Physics;

public class JmePhysics implements Physics {

    private final Application app;
    private BulletAppState bullet;

    public JmePhysics(Application app) {
        this.app = app;
    }

    public void setBullet(BulletAppState bullet) {
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

    // ✅ Novo método helper
    public void addStaticMesh(Spatial s) {
        if (s == null || bullet == null) return;

        CollisionShape shape;
        if (s instanceof Node node) {
            shape = CollisionShapeFactory.createMeshShape(node);
        } else {
            shape = CollisionShapeFactory.createMeshShape(s);
        }

        RigidBodyControl rbc = new RigidBodyControl(shape, 0f); // massa 0 = estático
        s.addControl(rbc);
        bullet.getPhysicsSpace().add(rbc);
    }

    @Override
    public void applyImpulse(long entityId, Vector3f impulse, Vector3f relPos) {
        Spatial s = SceneRegistry.get(entityId);
        if (s == null) return;
        RigidBodyControl rbc = s.getControl(RigidBodyControl.class);
        if (rbc != null) {
            rbc.applyImpulse(impulse, relPos != null ? relPos : Vector3f.ZERO);
        }
    }

    @Override
    public boolean raycast(Vector3f from, Vector3f to) {
        if (bullet == null) return false;
        var results = bullet.getPhysicsSpace().rayTest(from, to);
        return results != null && !results.isEmpty();
    }
}
