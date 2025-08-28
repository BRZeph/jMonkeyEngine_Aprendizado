package me.brzeph.infra.jme.adapter.physics;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CharacterPhysicsAdapter {

    private final BulletAppState bullet;
    private final Map<String, BetterCharacterControl> controls = new HashMap<>();

    public CharacterPhysicsAdapter(BulletAppState bullet) {
        this.bullet = bullet;
    }

    public void registerCharacter(Player player, Spatial model) {
        if (bullet == null || player == null || model == null) return;

        BetterCharacterControl control = new BetterCharacterControl(1.5f, 6f, 80f);
        model.addControl(control);

        // posição inicial no spatial
        if (player.getPosition() != null) {
            model.setLocalTranslation(player.getPosition());
        }

        bullet.getPhysicsSpace().add(control);
        controls.put(player.getId(), control);
    }

    public void moveCharacter(Player player, Vector3f walkDir) {
        BetterCharacterControl control = controls.get(player.getId());
        if (control != null) {
            control.setWalkDirection(walkDir);

            // sincroniza posição lógica com a do Spatial
            player.setPosition(control.getSpatial().getWorldTranslation().clone());
            player.setRotation(control.getSpatial().getWorldRotation().clone());
        }
    }

    public void jumpCharacter(Player player) {
        BetterCharacterControl control = controls.get(player.getId());
        if (control != null) {
            control.jump();
        }
    }

    public void removeCharacter(Player player) {
        BetterCharacterControl control = controls.remove(player.getId());
        if (control != null) {
            bullet.getPhysicsSpace().remove(control);
        }
    }
}


