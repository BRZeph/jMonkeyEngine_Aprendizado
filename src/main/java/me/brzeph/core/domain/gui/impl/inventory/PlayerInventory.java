package me.brzeph.core.domain.gui.impl.inventory;

import me.brzeph.core.domain.entity.item.ItemCategory;
import me.brzeph.core.domain.entity.item.ItemInstance;

import java.util.*;

import static me.brzeph.core.constants.ItemConstants.COIN_DEF;


public final class PlayerInventory implements InventoryPort {

    public static final int COMMON_CAPACITY = 36;

    private final EnumMap<ItemCategory.EquipSlot, ItemInstance> equipment =
            new EnumMap<>(ItemCategory.EquipSlot.class);

    private final ArrayList<ItemInstance> common = new ArrayList<>(COMMON_CAPACITY);

    // Especiais
    private int gold = 0;                              // slot CURRENCY (apenas ouro)
    private final ArrayList<ItemInstance> craftingBag = new ArrayList<>(); // conteúdo da “bag” de crafting

    // Slots equipáveis (tudo exceto CURRENCY, CRAFTING_BAG e COMMON_SLOT)
    private static final Set<ItemCategory.EquipSlot> EQUIPPABLE_SLOTS = EnumSet.of(
            ItemCategory.EquipSlot.HEAD, ItemCategory.EquipSlot.CHEST,
            ItemCategory.EquipSlot.LEGGINGS, ItemCategory.EquipSlot.BOOTS,
            ItemCategory.EquipSlot.GAUNTLET, ItemCategory.EquipSlot.BRACER,
            ItemCategory.EquipSlot.TRINKET1, ItemCategory.EquipSlot.TRINKET2,
            ItemCategory.EquipSlot.TRINKET3, ItemCategory.EquipSlot.TRINKET4,
            ItemCategory.EquipSlot.MAIN_HAND, ItemCategory.EquipSlot.OFF_HAND,
            ItemCategory.EquipSlot.POTION1, ItemCategory.EquipSlot.POTION2, ItemCategory.EquipSlot.POTION3
    );

    public PlayerInventory() {
        // inicializa inventário comum com nulls
        for (int i = 0; i < COMMON_CAPACITY; i++)
            common.add(null);
        // inicializa equipáveis como vazios
        for (ItemCategory.EquipSlot s : EQUIPPABLE_SLOTS)
            equipment.put(s, null);
    }

    // ==================== InventoryPort (leitura para UI) ====================

    @Override
    public ItemInstance getEquip(ItemCategory.EquipSlot slot) {
        return EQUIPPABLE_SLOTS.contains(slot) ? equipment.get(slot) : null;
    }

    @Override
    public int commonCapacity() { return COMMON_CAPACITY; }

    @Override
    public ItemInstance getCommon(int index) {
        rangeCheck(index);
        return common.get(index);
    }

    @Override
    public int goldAmount() { return gold; }

    @Override
    public ItemInstance currencyItem() {
        return new ItemInstance(COIN_DEF, goldAmount()).withQuantity(goldAmount());
    }

    @Override
    public ItemInstance craftingBagSummary() { return null; }     // opcional: item “saco” virtual

    // ==================== Mutação básica (para serviços do jogo) ====================

    public void setEquip(ItemCategory.EquipSlot slot, ItemInstance item) {
        if (!EQUIPPABLE_SLOTS.contains(slot))
            throw new IllegalArgumentException("Slot não equipável: " + slot);
        equipment.put(slot, item);
    }

    public void clearEquip(ItemCategory.EquipSlot slot) { setEquip(slot, null); }

    public void setCommon(int index, ItemInstance item) {
        rangeCheck(index);
        common.set(index, item);
    }

    public void swapCommon(int a, int b) {
        rangeCheck(a);
        rangeCheck(b);
        ItemInstance tmp = common.get(a);
        common.set(a, common.get(b));
        common.set(b, tmp);
    }

    public void addGold(int amount) { gold = Math.max(0, gold + amount); }
    public int setGold(int amount) { gold = Math.max(0, amount); return gold; }

    public List<ItemInstance> getCraftingBag() { return Collections.unmodifiableList(craftingBag); }
    public void addToCraftingBag(ItemInstance item) { if (item != null) craftingBag.add(item); }
    public boolean removeFromCraftingBag(ItemInstance item) { return craftingBag.remove(item); }
    public void clearCraftingBag() { craftingBag.clear(); }

    // ==================== Helpers ====================

    private static void rangeCheck(int idx) {
        if (idx < 0 || idx >= COMMON_CAPACITY)
            throw new IndexOutOfBoundsException("idx=" + idx);
    }
}

