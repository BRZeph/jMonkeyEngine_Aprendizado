package me.brzeph.infra.events.items;

import me.brzeph.core.domain.entity.item.DroppedItem;

public record DropItemEvent(DroppedItem item) {
}
