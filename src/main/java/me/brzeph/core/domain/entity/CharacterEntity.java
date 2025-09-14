package me.brzeph.core.domain.entity;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public abstract class CharacterEntity extends GameEntity {
    protected String name;
    protected CharacterStats stats; // level, hp, mp, maxSeeDistance etc.

    protected CharacterMovement movementStats = new CharacterMovement();

    public CharacterEntity(EntityType type, Vector3f pos, Quaternion rot,
                           String name, CharacterStats stats) {
        super(type, pos, rot);
        this.name = name;
        this.stats = stats;
    }

    public boolean isAlive(){
        return stats.getHp() > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CharacterStats getStats() {
        return stats;
    }

    public void setStats(CharacterStats stats) {
        this.stats = stats;
    }

    public CharacterMovement getMovementStats() {
        return movementStats;
    }

    public static final class CharacterMovement {
        public static final float walkSpeed = 3.5f;
        public static final float runSpeed  = 6.0f;
        public static final float accel     = 20f;
        public static final float turnRate  = 540f; // graus/s
        public static final float jumpHeight = 1.2f; // metros (intenção de gameplay)

        public float getWalkSpeed() {
            return walkSpeed;
        }

        public float getRunSpeed() {
            return runSpeed;
        }

        public float getAccel() {
            return accel;
        }

        public float getTurnRate() {
            return turnRate;
        }

        public float getJumpHeight() {
            return jumpHeight;
        }
    }
}

