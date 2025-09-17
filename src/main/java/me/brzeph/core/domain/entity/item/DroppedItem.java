package me.brzeph.core.domain.entity.item;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.GameEntity;

import java.time.Instant;

public class DroppedItem extends GameEntity {

    private final ItemInstance item; // payload do domínio
    private final float pickupRadius;
    private final Instant despawnAt; // null => não expira
    private final String droppedByCharacterId; // opcional

    public DroppedItem(EntityType type,
                       Vector3f position,
                       Quaternion rotation,
                       ItemInstance item,
                       float pickupRadius,
                       Instant despawnAt,
                       String droppedByCharacterId) { // TODO: Start using this parameter.
        super(type, position, rotation);
        this.item = item;
        this.pickupRadius = pickupRadius;
        this.despawnAt = despawnAt;
        this.droppedByCharacterId = droppedByCharacterId;
    }

    public void setQuantity(int amount) {
        item.setQuantity(amount);
    }

    public void addQuantity(int amount) {
        item.setQuantity(item.quantity() + amount);
    }

    public ItemInstance getItemInstance() { return item; }
    public Instant getDeSpawnAt() { return despawnAt; }
    public String getDroppedByCharacterId() { return droppedByCharacterId; }
}

