package me.brzeph.core.domain.gui.core.events;

import me.brzeph.core.domain.entity.item.InventoryItem;

// Coloque junto dos outros eventos de UI
public record UIDragStartEvent(String screenId, String widgetId, float startX, float startY, int button, InventoryItem item) {}
