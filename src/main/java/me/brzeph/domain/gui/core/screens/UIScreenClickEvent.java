package me.brzeph.domain.gui.core.screens;

import me.brzeph.domain.entity.item.ItemStack;
import me.brzeph.domain.gui.core.widgets.UIInventorySlot;

/** Eventos opcionais com screenId incluído (além dos OnClickEvent/OnHoldEvent já existentes). */
public record UIScreenClickEvent(String screenId, UIInventorySlot widget, float x, float y, int button, ItemStack item) {}
