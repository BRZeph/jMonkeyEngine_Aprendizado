package me.brzeph.core.service;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import me.brzeph.core.domain.entity.Player;

public class PlayerService {
    public static Vector3f calculateWalkDir(
            Camera cam, Player player,
            boolean movingForward, boolean movingBackward,
            boolean movingLeft, boolean movingRight
    ) {
        Vector3f fwd  = cam.getDirection().clone(); fwd.y = 0f;  fwd.normalizeLocal();
        Vector3f left = cam.getLeft().clone();      left.y = 0f; left.normalizeLocal();

        Vector3f wish = new Vector3f();
        if (movingForward)  wish.addLocal(fwd);
        if (movingBackward) wish.subtractLocal(fwd);
        if (movingLeft)     wish.addLocal(left);
        if (movingRight)    wish.subtractLocal(left);

        if (wish.lengthSquared() > 0f) {
            wish.normalizeLocal();
            wish.multLocal(player.getStats().getSpeed());
        }
        return wish;
    }
}

