package me.brzeph.core.domain.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public abstract class Character extends GameEntity {
    protected String name;
    protected CharacterStats stats; // encapsula level, hp, mp, speed, runningSpeed, maxSeeDistance
    protected float height;
    protected float weight;
    protected float jumpForce;
    /*
        Talvez encapsular características físicas como height, weight e jumpForce em CharacterPhysicsStats.
     */
    public Character(Vector3f position, Quaternion rotation, String name,
                     CharacterStats stats,
                     float height, float weight, float jumpForce) {
        super(position, rotation);
        this.name = name;
        this.stats = stats;
        this.height = height;
        this.weight = weight;
        this.jumpForce = jumpForce;
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

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getJumpForce() {
        return jumpForce;
    }

    public void setJumpForce(float jumpForce) {
        this.jumpForce = jumpForce;
    }

    public CharacterStats getStats() {
        return stats;
    }

    public void setStats(CharacterStats stats) {
        this.stats = stats;
    }
}
