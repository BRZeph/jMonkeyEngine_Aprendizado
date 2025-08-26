package me.brzeph.app.systems;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.JumpRequestedEvent;
import me.brzeph.infra.events.MoveKeyEvent;

public class MovementSystem extends BaseAppState {

    private final EventBus bus;
    private final BetterCharacterControl playerControl;

    private boolean forward, backward, left, right;

    public MovementSystem(EventBus bus, BetterCharacterControl playerControl) {
        this.bus = bus;
        this.playerControl = playerControl;
    }

    @Override
    protected void initialize(Application app) {
        bus.subscribe(MoveKeyEvent.class, this::onMoveKey);
        bus.subscribe(JumpRequestedEvent.class, this::onJump);
    }

    private void onMoveKey(MoveKeyEvent e) {
        if (e.playerId() != 1L) return;
        switch (e.direction()) {
            case FORWARD  -> forward  = e.pressed();
            case BACKWARD -> backward = e.pressed();
            case LEFT     -> left     = e.pressed();
            case RIGHT    -> right    = e.pressed();
        }
    }

    private void onJump(JumpRequestedEvent e) {
        if (e.playerId() != 1L) return;
        if (playerControl.isOnGround()) { // só pula se está no chão
            playerControl.jump();
        }
    }

    @Override
    public void update(float tpf) {
        Vector3f walkDir = new Vector3f();
        if (forward)  walkDir.addLocal(0, 0, -1);
        if (backward) walkDir.addLocal(0, 0,  1);
        if (left)     walkDir.addLocal(-1, 0, 0);
        if (right)    walkDir.addLocal( 1, 0, 0);

        if (walkDir.lengthSquared() > 0) {
            walkDir.normalizeLocal().multLocal(5f); // velocidade
        }

        playerControl.setWalkDirection(walkDir);
    }

    @Override protected void cleanup(Application app) {}
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}


