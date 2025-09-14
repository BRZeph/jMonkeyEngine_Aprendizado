package me.brzeph.core.domain.gui.core.events;

import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.gui.core.widgets.UIInventorySlot;

public record UIDragMoveEvent (String screenId, String widgetId, float x, float y, float dx, float dy, InventoryItem item) {}
