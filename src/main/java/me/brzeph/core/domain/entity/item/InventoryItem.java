package me.brzeph.core.domain.entity.item;

import me.brzeph.core.domain.gui.core.others.Color;

// Novo arquivo (ou no mesmo pacote dos itens)
public final class InventoryItem {
    private final ItemDefinition definition;
    private final float quantity;

    public InventoryItem(ItemDefinition definition, float quantity){
        this.definition = definition;
        this.quantity = Math.max(1, quantity);
    }

    public ItemDefinition definition(){ return definition; }
    public float quantity(){ return quantity; }
}
