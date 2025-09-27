package me.brzeph.domain.gui.core.widgets;

import me.brzeph.domain.entity.item.ItemCategory;
import me.brzeph.domain.entity.item.ItemInstance;
import me.brzeph.domain.gui.core.others.Color;
import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;

import java.util.ArrayList;
import java.util.List;

import static me.brzeph.domain.gui.core.others.Rect.strokeRect;

public final class UIInventorySlot extends Widget<UIInventorySlot> {

    private final UIItemIcon icon = new UIItemIcon();
    private ItemInstance item;
    private ItemCategory.EquipSlot accepts = ItemCategory.EquipSlot.COMMON_SLOT;
    private int index = -1;
    private boolean highlight, dragOver;
    private static final List<UIInventorySlot> registeredSlots = new ArrayList<>();

    public UIInventorySlot() {
        registeredSlots.add(this);
    }

    public List<UIInventorySlot> getInventorySlot(ItemCategory.EquipSlot s){
        List<UIInventorySlot> slots = new ArrayList<>();
        for(UIInventorySlot slot : registeredSlots){
            if(slot.getAccepts() == s){
                slots.add(slot);
            }
        }
        return slots;
    }

    // config
    public UIInventorySlot index(int i){
        this.index = i;
        return this;
    }

    public UIInventorySlot accepts(ItemCategory.EquipSlot k){
        this.accepts = k;
        return this;
    }

    // item API
    public UIInventorySlot setItem(ItemInstance it){
        this.item = it;
        icon.item(it);
        return this;
    }

    public ItemInstance getItem(){
        return item;
    }

    public boolean isEmpty(){
        return item == null;
    }

    public ItemCategory.EquipSlot getAccepts() {
        return accepts;
    }

    public boolean canAccept(ItemInstance it){
        if (it == null) return true;
        return accepts == it.definition().equipSlot() || accepts == ItemCategory.EquipSlot.COMMON_SLOT;
    }

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

    public int getIndex() {
        return index;
    }
}

