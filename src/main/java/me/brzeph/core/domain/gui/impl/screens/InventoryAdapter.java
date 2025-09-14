package me.brzeph.core.domain.gui.impl.screens;

import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.entity.item.ItemCategory;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.gui.core.layout.ColumnLayout;
import me.brzeph.core.domain.gui.core.layout.LayoutParams;
import me.brzeph.core.domain.gui.core.layout.RowLayout;
import me.brzeph.core.domain.gui.core.widgets.Panel;
import me.brzeph.core.domain.gui.core.widgets.UIGrid;
import me.brzeph.core.domain.gui.core.widgets.UIInventorySlot;
import me.brzeph.core.domain.gui.impl.inventory.InventoryPort;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static me.brzeph.core.domain.entity.item.ItemCategory.EquipSlot.*;

public final class InventoryAdapter {
    private InventoryAdapter(){}

    public static final ItemCategory.EquipSlot[] ARMOR = { HEAD, CHEST, LEGGINGS, BOOTS, GAUNTLET, BRACER };
    public static final ItemCategory.EquipSlot[] TRINKETS = { TRINKET1, TRINKET2, TRINKET3, TRINKET4 };
    public static final ItemCategory.EquipSlot[] HANDS = { MAIN_HAND, OFF_HAND };
    public static final ItemCategory.EquipSlot[] POTIONS = { POTION1, POTION2, POTION3 };

    public static InventoryItem toInventoryItem(ItemInstance ii){
        if (ii == null) return null;
        int qty = 1;
        try { var m = ii.getClass().getMethod("quantity"); qty = (int)m.invoke(ii); }
        catch (ReflectiveOperationException ignore) { qty = 1; }
        return new InventoryItem(ii.def(), qty);
    }

    // ---------- UI slots ----------
    public static UIInventorySlot makeTypedSlot(String invKey, ItemCategory.EquipSlot accepts, ItemInstance ii){
        return new UIInventorySlot()
                .accepts(accepts)
                .setItem(toInventoryItem(ii))
                .id(invKey + ".slot." + accepts.name());
    }

    public static UIInventorySlot makeCommonSlot(String invKey, int index, ItemInstance ii){
        return new UIInventorySlot()
                .accepts(ItemCategory.EquipSlot.COMMON_SLOT)
                .index(index)
                .setItem(toInventoryItem(ii))
                .id(invKey + ".slot.common." + index);
    }

    // ---------- Overloads com InventoryPort ----------
    public static Panel makeEquipmentPanel(String invKey, InventoryPort port, float vgap, float hgap){
        Panel col = new Panel().layout(new ColumnLayout(vgap, 0f));

        col.add(makeRow(invKey, ARMOR,    port, hgap).layout(LayoutParams.wrap()));
        col.add(makeRow(invKey, TRINKETS, port, hgap).layout(LayoutParams.wrap()));
        col.add(makeRow(invKey, HANDS,    port, hgap).layout(LayoutParams.wrap()));
        col.add(makeRow(invKey, POTIONS,  port, hgap).layout(LayoutParams.wrap()));

        Panel special = new Panel().layout(new RowLayout(hgap, 0f));
        special.add(makeTypedSlot(invKey, ItemCategory.EquipSlot.CURRENCY,     port.currencyItem()).layout(LayoutParams.wrap()));
        special.add(makeTypedSlot(invKey, ItemCategory.EquipSlot.CRAFTING_BAG, port.craftingBagSummary()).layout(LayoutParams.wrap()));
        col.add(special.layout(LayoutParams.wrap()));

        return col;
    }

    private static Panel makeRow(String invKey, ItemCategory.EquipSlot[] arr, InventoryPort port, float gap){
        Panel row = new Panel().layout(new RowLayout(gap, 0f));
        for (var s : arr){
            row.add(makeTypedSlot(invKey, s, port.getEquip(s)).layout(LayoutParams.wrap()));
        }
        return row;
    }

    public static void fillCommonGrid(UIGrid grid, InventoryPort port, String invKey, int columns, float gap){
        grid.columns(columns).gap(gap);
        int n = port.commonCapacity();
        for (int i=0;i<n;i++){
            grid.add(makeCommonSlot(invKey, i, port.getCommon(i)).layout(LayoutParams.wrap()));
        }
    }

    // ---------- refresh pontual ----------
    public static void refreshCommonSlot(UIInventorySlot slot, InventoryPort port, int index){
        slot.setItem(toInventoryItem(port.getCommon(index)));
    }
    public static void refreshEquipSlot(UIInventorySlot slot, InventoryPort port, ItemCategory.EquipSlot s){
        slot.setItem(toInventoryItem(port.getEquip(s)));
    }
}
