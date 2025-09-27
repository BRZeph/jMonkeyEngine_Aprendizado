package me.brzeph.domain.gui.impl.inventory;

import me.brzeph.domain.entity.item.ItemCategory;
import me.brzeph.domain.entity.item.ItemInstance;

public interface InventoryPort {
    // Equip slots (HEAD, MAIN_HAND, POTION1, etc.)
    ItemInstance getEquip(ItemCategory.EquipSlot slot);

    // Inventário comum
    int commonCapacity();
    ItemInstance getCommon(int index);
    ItemInstance getCommon(int row, int col);

    // Especiais
    int goldAmount();
    ItemInstance currencyItem();
    ItemInstance craftingBagSummary();
}

