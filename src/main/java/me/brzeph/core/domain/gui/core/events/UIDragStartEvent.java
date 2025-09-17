package me.brzeph.core.domain.gui.core.events;

import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.gui.core.widgets.UIInventorySlot;

// Coloque junto dos outros eventos de UI
public record UIDragStartEvent(String screenId, UIInventorySlot widget, float startX, float startY, int button, InventoryItem item) {}
