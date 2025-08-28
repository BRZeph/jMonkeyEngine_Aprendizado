package me.brzeph.core.service;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.infra.jme.adapter.utils.InputAction;

public class PlayerService {
    public Vector3f calculateWalkDir(
            Camera cam, Player player,
            boolean movingForward, boolean movingBackward,
            boolean movingLeft, boolean movingRight
    ) {
        Vector3f walkDir = new Vector3f();
        walkDir.set(0, 0, 0);
        if (movingForward) {
            walkDir.addLocal(cam.getDirection());
        }
        if (movingBackward) {
            walkDir.addLocal(cam.getDirection().negate());
        }
        if (movingLeft) {
            walkDir.addLocal(cam.getLeft());
        }
        if (movingRight) {
            walkDir.addLocal(cam.getLeft().negate());
        }
        walkDir.normalizeLocal().multLocal(player.getSpeed());
        walkDir.setY(0);
        return walkDir;
    }

    public boolean canJump(Player player) {
        return player.getHp() > 0; // TODO: Checar se está tocando o chão.
    }
}

