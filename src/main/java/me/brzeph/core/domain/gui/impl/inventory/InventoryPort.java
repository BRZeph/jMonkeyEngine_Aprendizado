package me.brzeph.core.domain.gui.impl.inventory;

import me.brzeph.core.domain.entity.item.ItemCategory;
import me.brzeph.core.domain.entity.item.ItemInstance;

public interface InventoryPort {
    // Equip slots (HEAD, MAIN_HAND, POTION1, etc.)
    ItemInstance getEquip(ItemCategory.EquipSlot slot);

    // Inventário comum
    int commonCapacity();
    ItemInstance getCommon(int index);

    // Especiais
    int goldAmount();                               // total de ouro (para o info card)
    ItemInstance currencyItem();          // opcional: item “moeda” para o slot visual
    ItemInstance craftingBagSummary();    // opcional: item “saco” para o slot visual
}

