package me.brzeph.core.domain.gui.core.screens;

import me.brzeph.core.domain.entity.item.InventoryItem;

/** Eventos opcionais com screenId incluído (além dos OnClickEvent/OnHoldEvent já existentes). */
public record UIScreenClickEvent(String screenId, String widgetId, float x, float y, int button, InventoryItem item) {}
