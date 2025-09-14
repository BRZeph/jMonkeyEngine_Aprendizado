package me.brzeph.core.domain.entity;

import com.jme3.bullet.control.*;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import me.brzeph.infra.repository.GameEntityRepository;

import static me.brzeph.core.constants.CollisionConstants.*;

public abstract class GameEntity {
    protected String id;
    protected EntityType type;
    protected Vector3f position;
    protected Quaternion rotation;
    protected Node characterNode;
    protected final Vector3f spawnPoint;
    protected final Quaternion spawnRotation;
    protected float spawnScale = 1f;

    public GameEntity(EntityType type, Vector3f position, Quaternion rotation) {
        this.type = type;
        this.id = null;
        this.position = position;
        this.rotation = rotation;
        this.id = GameEntityRepository.register(this);
        this.spawnPoint = position.clone();
        this.spawnRotation = rotation.clone();
    }

    public float getHeight() {
        return type.blueprint().visual().height();
    }

    public PhysicsControl getControl(){
        String controlType = type.blueprint().physics().body().getControlString();
        return switch (controlType) {
            case CONTROL_TYPE_BCC -> characterNode.getControl(BetterCharacterControl.class);
            case CONTROL_TYPE_RBC -> characterNode.getControl(RigidBodyControl.class);
            case CONTROL_TYPE_GC -> characterNode.getControl(GhostControl.class);
            default -> throw new RuntimeException("Unknown control type: " + controlType);
        };
    }

    public String getControlString(){
        return type.blueprint().physics().body().getControlString();
    }

    public Node getCharacterNode() {
        return characterNode;
    }

    public void setCharacterNode(Node characterNode) {
        this.characterNode = characterNode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public Vector3f getSpawnPoint() {
        return spawnPoint;
    }

    public Quaternion getSpawnRotation() {
        return spawnRotation;
    }

    public float getSpawnScale() {
        return spawnScale;
    }
}
