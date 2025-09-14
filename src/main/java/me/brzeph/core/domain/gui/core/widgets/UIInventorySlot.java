package me.brzeph.core.domain.gui.core.widgets;

import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.entity.item.ItemCategory;
import me.brzeph.core.domain.gui.core.others.Color;
import me.brzeph.core.domain.gui.core.others.Rect;
import me.brzeph.core.domain.gui.core.others.Size;

import java.util.Optional;

import static me.brzeph.core.constants.ItemConstants.COIN_ITEM_ID;
import static me.brzeph.core.domain.gui.core.others.Rect.strokeRect;

public final class UIInventorySlot extends Widget<UIInventorySlot> {

    private final UIItemIcon icon = new UIItemIcon();
    private InventoryItem item;
    private ItemCategory.EquipSlot accepts = ItemCategory.EquipSlot.COMMON_SLOT;
    private int index = -1;                    // para saber qual slot é (ex.: 0..N-1)
    private boolean highlight, dragOver;

    // config
    public UIInventorySlot index(int i){ this.index = i; return this; }
    public UIInventorySlot accepts(ItemCategory.EquipSlot k){ this.accepts = k; return this; }

    // item API
    public UIInventorySlot setItem(InventoryItem it){ this.item = it; icon.item(it); return this; }
    public InventoryItem getItem(){ return item; }
    public boolean isEmpty(){ return item == null; }

    // regra simples de aceitação
    public boolean canAccept(InventoryItem it){
        if (it == null) return true; // limpar o slot

        return switch (accepts) {
            case COMMON_SLOT -> true; // sempre cabe no inventário comum

            case CURRENCY ->
                // aceite apenas "ouro". Ajuste conforme seu ItemDefinition
                    it.definition().category() == ItemCategory.CURRENCY
                            && COIN_ITEM_ID.equalsIgnoreCase(it.definition().id().value());
            case CRAFTING_BAG ->
                // aceite apenas materiais de crafting (o "saco" agrega internamente)
                    it.definition().category() == ItemCategory.CRAFTING_MATERIAL;
            case POTION1, POTION2, POTION3 -> it.definition().category() == ItemCategory.POTION;
            default ->
                // slots de equipamento: requer EquipSpec que permita esse slot
                    it.definition().equip()
                            .map(ItemCategory.EquipSpec::allowedSlots)
                            .map(allowed -> allowed.contains(accepts))
                            .orElse(false);
        };
    }

    // tamanho celula
    private static final float CELL = 64f; // TODO: move this inside ItemConstants or GUIConstants.
    @Override
    public Size measure(float maxW, float maxH){ return new Size(CELL, CELL); }

    // *** restringe que ninguém "adicione" filhos arbitrários neste widget ***
    @Override
    public UIInventorySlot add(Widget<?> w){
        // impede adicionar outros filhos além do ícone interno
        throw new UnsupportedOperationException("UIInventorySlot não aceita filhos externos");
    }

    @Override
    public void draw(){
        if (!isVisible()) return;

        Rect r = bounds();
        Color bg = Color.rgba(0.12f,0.12f,0.14f, 1f);
        g().drawRect(r, bg, 8f);

        if (highlight) strokeRect(g(), r, Color.rgba(1f,1f,1f,0.15f), 1f);
        if (dragOver)  strokeRect(g(), r, Color.rgba(0.3f,0.8f,1f,0.80f), 2f);

        icon.setBounds(r);
        icon.draw();
    }

    // ganchos para o controller mudar visual rapidamente
    public void setHighlight(boolean v){ this.highlight = v; }
    public void setDragOver(boolean v){ this.dragOver = v; }

    // helper para IDs estáveis por convenção
    public UIInventorySlot stableId(String invKey){
        // ex.: inv.slot.12
        return this.id(invKey + ".slot." + index);
    }
}

