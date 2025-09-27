package me.brzeph.domain.gui.impl.screens;

import me.brzeph.domain.entity.item.ItemCategory;
import me.brzeph.domain.entity.item.ItemInstance;
import me.brzeph.domain.gui.core.layout.ColumnLayout;
import me.brzeph.domain.gui.core.layout.LayoutParams;
import me.brzeph.domain.gui.core.layout.RowLayout;
import me.brzeph.domain.gui.core.widgets.Panel;
import me.brzeph.domain.gui.core.widgets.UIGrid;
import me.brzeph.domain.gui.core.widgets.UIInventorySlot;
import me.brzeph.domain.gui.core.widgets.Widget;
import me.brzeph.domain.gui.impl.inventory.InventoryPort;

import java.util.List;

import static me.brzeph.domain.entity.item.ItemCategory.*;
import static me.brzeph.domain.gui.impl.inventory.PlayerInventory.COLUMNS;
import static me.brzeph.domain.gui.impl.inventory.PlayerInventory.ROWS;

public final class InventoryAdapter {
    private InventoryAdapter(){}

    public static ItemInstance toItemInstance(ItemInstance ii){
        if (ii == null) return null;
        return new ItemInstance(ii.definition(), ii.quantity());
    }

    // ---------- UI slots ----------
    public static UIInventorySlot makeTypedSlot(String invKey, ItemCategory.EquipSlot accepts, ItemInstance ii){
        return new UIInventorySlot()
                .accepts(accepts)
                .setItem(toItemInstance(ii))
                .id(invKey + ".slot." + accepts.name());
    }

    public static UIInventorySlot makeCommonSlot(String invKey, int index, ItemInstance ii){
        return new UIInventorySlot()
                .accepts(ItemCategory.EquipSlot.COMMON_SLOT)
                .index(index)
                .setItem(toItemInstance(ii))
                .id(invKey + ".slot.common." + index);
    }

    // ---------- Overloads com InventoryPort ----------
    public static Panel makeEquipmentPanel(String invKey, InventoryPort port, float vgap, float hgap){
        Panel col = new Panel().layout(new ColumnLayout(vgap, 0f));

        Panel special = new Panel().layout(new RowLayout(hgap, 0f));
        Widget<UIInventorySlot> gold = makeTypedSlot(invKey, ItemCategory.EquipSlot.CURRENCY, port.currencyItem()).layout(LayoutParams.wrap());
        gold.setStartDrag(false);
        special.add(gold);
        Widget<UIInventorySlot> craftingBag = makeTypedSlot(invKey, ItemCategory.EquipSlot.CRAFTING_BAG, port.craftingBagSummary()).layout(LayoutParams.wrap());
        craftingBag.setStartDrag(false);
        special.add(craftingBag);
        col.add(special.layout(LayoutParams.wrap()));

        col.add(makeRow(invKey, TRINKET.getSlots(), port, hgap).layout(LayoutParams.wrap()));
        col.add(makeRow(invKey, HANDS.getSlots(),   port, hgap).layout(LayoutParams.wrap()));
        col.add(makeRow(invKey, POTION.getSlots(),  port, hgap).layout(LayoutParams.wrap()));
        col.add(makeRow(invKey, ARMOR.getSlots(),   port, hgap).layout(LayoutParams.wrap()));

        return col;
    }

    private static Panel makeRow(String invKey, List<EquipSlot> arr, InventoryPort port, float gap){
        Panel row = new Panel().layout(new RowLayout(gap, 0f));
        for (var s : arr){
            row.add(makeTypedSlot(invKey, s, port.getEquip(s)).layout(LayoutParams.wrap()));
        }
        return row;
    }

    public static void fillCommonGrid(UIGrid grid, InventoryPort port, String invKey){
        grid.columns(COLUMNS).gap(ROWS);
        int n = port.commonCapacity();
        for (int i=0;i<n;i++){
            grid.add(makeCommonSlot(invKey, i, port.getCommon(i)).layout(LayoutParams.wrap()));
        }
    }
}
