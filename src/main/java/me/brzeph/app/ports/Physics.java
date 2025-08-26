package me.brzeph.app.ports;

import com.jme3.math.Vector3f;

public interface Physics {
    void addRigidBody(long entityId, float mass);
    void applyImpulse(long entityId, Vector3f impulse, Vector3f relPos);
    boolean raycast(Vector3f from, Vector3f to);
}
