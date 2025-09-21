package me.brzeph.core.domain.gui.core.events;

import me.brzeph.core.domain.entity.item.ItemInstance;

public record UIDragMoveEvent (String screenId, String widgetId, float x, float y, float dx, float dy, ItemInstance item) {}
