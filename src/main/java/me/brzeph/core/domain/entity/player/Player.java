package me.brzeph.core.domain.entity.player;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterEntity;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.gui.impl.inventory.PlayerInventory;

public class Player extends CharacterEntity {

    private final PlayerInventory inventory;

    public Player(EntityType type, Vector3f position, Quaternion rotation, String name,
                  CharacterStats stats) {
        super(type, position, rotation, name, stats);
        this.inventory = new PlayerInventory();
    }

    public PlayerInventory getInventory() { return inventory; }
}
