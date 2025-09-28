package me.brzeph.domain.gui.impl.inventory;

import me.brzeph.domain.entity.item.ItemCategory;
import me.brzeph.domain.entity.item.ItemStack;

public interface InventoryPort {
    // Equip slots (HEAD, MAIN_HAND, POTION1, etc.)
    ItemStack getEquip(ItemCategory.EquipSlot slot);

    // Inventário comum
    int commonCapacity();
    ItemStack getCommon(int index);
    ItemStack getCommon(int row, int col);

    // Especiais
    int goldAmount();
    ItemStack currencyItem();
    ItemStack craftingBagSummary();
}

