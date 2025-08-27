package me.brzeph.core.domain.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public abstract class GameEntity {
    protected String id;
    protected Vector3f position;
    protected Quaternion rotation;

    public GameEntity(Vector3f position, Quaternion rotation) {
        this.id = null;
        this.position = position;
        this.rotation = rotation;
    }

    public abstract boolean register();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }

    public Quaternion getRotation() { return rotation; }
    public void setRotation(Quaternion rotation) { this.rotation = rotation; }
}
