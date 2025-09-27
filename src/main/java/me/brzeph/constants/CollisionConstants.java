package me.brzeph.constants;

import com.jme3.bullet.collision.PhysicsCollisionObject;

public final class CollisionConstants {

    public static final class CG { // Os endereços são em bit: 2^n
        public static final int WORLD   = PhysicsCollisionObject.COLLISION_GROUP_01;
        public static final int PLAYER  = PhysicsCollisionObject.COLLISION_GROUP_02;
        public static final int MONSTER = PhysicsCollisionObject.COLLISION_GROUP_03;
        public static final int ITEM    = PhysicsCollisionObject.COLLISION_GROUP_04;
        public static final int SENSOR  = PhysicsCollisionObject.COLLISION_GROUP_05;
    }

    public static final String CONTROL_TYPE_BCC = "bcc";
    public static final String CONTROL_TYPE_RBC = "rbc";
    public static final String CONTROL_TYPE_GC  = "gc";
}
