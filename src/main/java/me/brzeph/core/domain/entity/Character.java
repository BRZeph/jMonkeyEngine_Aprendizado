package me.brzeph.core.domain.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public abstract class Character extends GameEntity {
    protected String name;
    protected int level;
    protected int hp;
    protected int mp;
    protected float speed;

    public Character(Vector3f position, Quaternion rotation,
                     String name, int level, int hp, int mp, float speed) {
        super(position, rotation);
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.mp = mp;
        this.speed = speed;
    }

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }

    public boolean isAlive() {
        return hp > 0;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
