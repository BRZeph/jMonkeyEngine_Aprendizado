package me.brzeph.app.systems.impl.collisionSystem.handlers;

import me.brzeph.core.domain.entity.item.DroppedItem;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.app.systems.impl.InventorySystem;
import me.brzeph.core.domain.gui.impl.inventory.InventorySystemInt;

public class ItemProximityService {

    static boolean attemptPlayerCollectItem(InventorySystem system, Player ea, DroppedItem eb) {
        InventorySystemInt.AddResult result = system.pickup(ea, eb);
        return result.added() != 0;
    }
}
