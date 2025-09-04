package me.brzeph.infra.events.items;

import me.brzeph.core.domain.item.DroppedItem;

public record DropItemEvent(DroppedItem item) {
}
