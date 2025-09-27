package me.brzeph.domain.gui.core.events;

import me.brzeph.domain.entity.item.ItemInstance;
import me.brzeph.domain.gui.core.widgets.UIInventorySlot;

// Coloque junto dos outros eventos de UI
public record UIDragStartEvent(String screenId, UIInventorySlot widget, float startX, float startY, int button, ItemInstance item) {}
