package me.brzeph.core.domain.item;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.core.domain.entity.CharacterStats;

public class DroppedItem extends Character {
    public DroppedItem(Vector3f position, Quaternion rotation, String name, CharacterStats stats, float height, float weight, float jumpForce) {
        super(position, rotation, name, stats, height, weight, jumpForce);
    }
}
