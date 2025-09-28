package me.brzeph.domain.gui.impl.inventory;

import me.brzeph.domain.entity.CharacterEntity;
import me.brzeph.domain.entity.item.DroppedItem;
import me.brzeph.domain.entity.item.ItemStack;

public interface InventorySystemInt {
    /** Tenta adicionar 'item' ao inventário do entity. Retorna quanto entrou e, se sobrar, o restante. */
    AddResult addItem(CharacterEntity player, ItemStack item, boolean autoequip);

    /** Atalho para coletar um drop do chão. */
    AddResult pickup(CharacterEntity player, DroppedItem drop);

    record AddResult(int added, ItemStack remainder) {}
}

