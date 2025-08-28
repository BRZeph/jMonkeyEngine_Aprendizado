package me.brzeph.core.service;

import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.Player;

public class PlayerService {

    public Vector3f calculateWalkDirection(Player player, boolean forward, boolean backward, boolean left, boolean right) {
        // morto não anda
        if (player.getHp() <= 0) {
            return Vector3f.ZERO;
        }

        Vector3f dir = new Vector3f();
        if (forward)  dir.addLocal(Vector3f.UNIT_Z);
        if (backward) dir.addLocal(Vector3f.UNIT_Z.negate());
        if (left)     dir.addLocal(Vector3f.UNIT_X.negate());
        if (right)    dir.addLocal(Vector3f.UNIT_X);

        return dir.normalizeLocal().multLocal(player.getSpeed());
    }

    public boolean canJump(Player player) {
        return player.getHp() > 0; // simples: só pula se vivo
    }
}

