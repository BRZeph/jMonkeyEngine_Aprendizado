package me.brzeph.core.domain.gui.core.screens;

import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.gui.core.widgets.UIInventorySlot;

/** Eventos opcionais com screenId incluído (além dos OnClickEvent/OnHoldEvent já existentes). */
public record UIScreenClickEvent(String screenId, UIInventorySlot widget, float x, float y, int button, ItemInstance item) {}
