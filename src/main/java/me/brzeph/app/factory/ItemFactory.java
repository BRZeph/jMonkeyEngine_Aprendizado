package me.brzeph.app.factory;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.item.DroppedItem;
import me.brzeph.domain.entity.item.ItemCategory.*;
import me.brzeph.domain.entity.item.ItemStack;

import static me.brzeph.constants.ItemConstants.PREFAB_RESOLVER;

public final class ItemFactory {
    private ItemFactory() {}

    public static DroppedItem createDropFrom(ItemStack ii,
                                             Vector3f pos,
                                             Quaternion rot) {
        var hintOpt = ii.definition().dropHint();
        EntityType type = hintOpt
                .map(h -> PREFAB_RESOLVER.apply(h.worldPrefabKey()))
                .orElseGet(() -> PREFAB_RESOLVER.apply("default_item_pickup"));

        float pickupRadius = hintOpt.map(DropHint::pickupRadius).orElse(1.0f);
        float life = hintOpt.map(DropHint::lifetimeSeconds).orElse(120f);

        return new DroppedItem(
                type, pos, rot, ii, pickupRadius,
                life <= 0 ? null : java.time.Instant.now().plusSeconds((long) life),
                null
        );
    }
}
