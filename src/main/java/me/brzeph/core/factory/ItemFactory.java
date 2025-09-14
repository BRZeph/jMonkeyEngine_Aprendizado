package me.brzeph.core.factory;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.item.DroppedItem;
import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.entity.item.ItemCategory.*;
import me.brzeph.core.domain.entity.item.ItemInstance;

import java.util.function.Function;

public final class ItemFactory {
    private ItemFactory() {}

    public static InventoryItem createInventoryItem(ItemInstance ii){
        return new InventoryItem(ii.def(), ii.quantity());
    }

    public static DroppedItem createDropFrom(ItemInstance ii,
                                             Vector3f pos,
                                             Quaternion rot,
                                             Function<String, EntityType> prefabResolver) {
        var hintOpt = ii.def().dropHint();
        EntityType type = hintOpt
                .map(h -> prefabResolver.apply(h.worldPrefabKey()))
                .orElseGet(() -> prefabResolver.apply("default_item_pickup"));

        float pickupRadius = hintOpt.map(DropHint::pickupRadius).orElse(1.0f);
        float life = hintOpt.map(DropHint::lifetimeSeconds).orElse(120f);

        return new DroppedItem(
                type, pos, rot, ii, pickupRadius,
                life <= 0 ? null : java.time.Instant.now().plusSeconds((long) life),
                null
        );
    }
}
