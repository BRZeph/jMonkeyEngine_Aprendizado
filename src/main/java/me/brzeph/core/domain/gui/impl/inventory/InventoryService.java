package me.brzeph.core.domain.gui.impl.inventory;

import me.brzeph.core.domain.entity.item.DroppedItem;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.entity.player.Player;

public interface InventoryService {
    /** Tenta adicionar 'item' ao inventário do player. Retorna quanto entrou e, se sobrar, o restante. */
    AddResult addItem(Player player, ItemInstance item);

    /** Atalho para coletar um drop do chão. */
    AddResult pickup(Player player, DroppedItem drop);

    record AddResult(int added, ItemInstance remainder) {}
}

