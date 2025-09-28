package me.brzeph.domain.gui.core.events;

import me.brzeph.domain.entity.item.ItemStack;
import me.brzeph.domain.gui.core.widgets.UIInventorySlot;

// Coloque junto dos outros eventos de UI
public record UIDragStartEvent(String screenId, UIInventorySlot widget, float startX, float startY, int button, ItemStack item) {}
