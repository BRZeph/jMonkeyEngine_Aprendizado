package me.brzeph.domain.entity.player;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.domain.entity.CharacterEntity;
import me.brzeph.domain.entity.CharacterStats;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.gui.impl.inventory.PlayerInventory;

public class Player extends CharacterEntity {

    private final PlayerInventory inventory;

    public Player(EntityType type, Vector3f position, Quaternion rotation, String name,
                  CharacterStats stats) {
        super(type, position, rotation, name, stats);
        this.inventory = new PlayerInventory();
    }

    public PlayerInventory getInventory() { return inventory; }
}
