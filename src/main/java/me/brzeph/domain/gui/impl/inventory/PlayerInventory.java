package me.brzeph.domain.gui.impl.inventory;

import com.jme3.anim.Armature;
import com.jme3.anim.SkinningControl;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.domain.entity.item.ItemCategory;
import me.brzeph.domain.entity.item.ItemStack;
import me.brzeph.domain.entity.player.Player;
import me.brzeph.domain.gui.core.widgets.UIInventorySlot;

import java.util.*;

import static me.brzeph.app.systems.impl.animationSystem.AnimationService.findControl;
import static me.brzeph.constants.ItemConstants.COIN_DEF;
import static me.brzeph.constants.ItemConstants.CRAFTING_BAG;


public final class PlayerInventory implements InventoryPort {

    public static final int ROWS = 4;
    public static final int COLUMNS = 9;
    public static final int COMMON_CAPACITY = ROWS * COLUMNS;

    private final EnumMap<ItemCategory.EquipSlot, ItemStack> equipment =
            new EnumMap<>(ItemCategory.EquipSlot.class);

    private final ItemStack[][] common = new ItemStack[ROWS][COLUMNS];

    private ItemStack currency;

    private final ItemStack craftingBag; // This is just to create the slot, the items are stored inside craftingBagContents
    private final Set<ItemStack> craftingBagContents;

    private final Player player;

    private Spatial equippedWeaponR = null;

    public PlayerInventory(Player player) {
        this.player = player;
        for(int r = 0; r < ROWS; r++) {
            for(int c = 0; c < COLUMNS; c++) {
                common[r][c] = null;
            }
        }

        currency = new ItemStack(COIN_DEF, 0);
        craftingBag = new ItemStack(CRAFTING_BAG, 0);
        craftingBagContents = new HashSet<>();

        List<ItemCategory.EquipSlot> ignore = List.of(
                ItemCategory.EquipSlot.CRAFTING_BAG,
                ItemCategory.EquipSlot.CURRENCY,
                ItemCategory.EquipSlot.COMMON_SLOT
        );

        for(ItemCategory.EquipSlot s : ItemCategory.EquipSlot.values()) {
            if (ignore.contains(s)) continue;
            equipment.put(s, null);
        }
    }

    @Override
    public ItemStack getEquip(ItemCategory.EquipSlot slot) {
        if (equipment.containsKey(slot)) return equipment.get(slot);
        return switch (slot) {
            case CURRENCY -> currency;
            case CRAFTING_BAG -> throw new IllegalArgumentException("Illegal argument for getEquip: CRAFTING_BAG");
            case COMMON_SLOT -> throw new IllegalArgumentException("Illegal argument for getEquip: COMMON_SLOT");
            default -> throw new IllegalArgumentException("Illegal argument for getEquip: " + slot);
        };
    }

    @Override
    public int commonCapacity() {
        return COMMON_CAPACITY;
    }

    @Override
    public ItemStack getCommon(int index) {
        int row = index/COLUMNS;
        int col = index%COLUMNS;
        return common[row][col];
    }

    @Override
    public ItemStack getCommon(int row, int col) {
        return common[row][col];
    }

    @Override
    public int goldAmount() {
        return currency.quantity();
    }

    @Override
    public ItemStack currencyItem() {
        return currency;
    }

    @Override
    public ItemStack craftingBagSummary() {
        return craftingBag;
    }

    public void addToCraftingBag(ItemStack item) {
        assert ItemCategory.CRAFTING_MATERIAL.contains(item.definition().equipSlot());
        if (craftingBagContents.contains(item)){
            String itemId = item.definition().id().itemId();
            String s;
            for (ItemStack i : craftingBagContents) {
                s = i.definition().id().itemId();
                if (Objects.equals(s, itemId)){
                    i.setQuantity(i.quantity() + item.quantity());
                }
            }
        } else {
            craftingBagContents.add(item);
        }
    }

    public void addGold(int qtd){
        this.currency.addQuantity(qtd);
    }

    public void setEquip(ItemCategory.EquipSlot slot, ItemStack item) {
        assert equipment.containsKey(item.definition().equipSlot());

        if (slot == ItemCategory.EquipSlot.MAIN_HAND && item.definition().equipSpec().isPresent()){
            if (item.definition().equipSpec().get().glbPath() != null){
                SkinningControl skinning   = findControl(player.getCharacterNode(), SkinningControl.class);
                Spatial weapon = ServiceLocator.get(AssetManager.class).loadModel("assets/glb/items/simple_sword.glb");
                Node handAttach  = skinning.getAttachmentsNode("Arm.R_socket_bone");
                handAttach.attachChild(weapon);
                equippedWeaponR = weapon;
            }
        }

        equipment.replace(slot, item);
    }

    public void setCommon(int index, ItemStack itemStack){
        assert index < COMMON_CAPACITY;

        int row = index/COLUMNS;
        int col = index%COLUMNS;
        common[row][col] = itemStack;
    }

    public ItemStack unEquip(ItemCategory.EquipSlot slot) {
        assert equipment.containsKey(slot);
        ItemStack i = equipment.get(slot);
        equipment.replace(slot, null);
        if (slot == ItemCategory.EquipSlot.MAIN_HAND && equippedWeaponR != null){
            equippedWeaponR.removeFromParent();
            equippedWeaponR = null;
        }
        return i;
    }

    private void swapCommon(int a, int b){
        assert a < COMMON_CAPACITY && b < COMMON_CAPACITY;

        int rowA = a/COLUMNS;
        int colA = a%COLUMNS;
        int rowB = b/COLUMNS;
        int colB = b%COLUMNS;

        ItemStack temp = common[rowA][colA];
        common[rowA][colA] = common[rowB][colB];
        common[rowB][colB] = temp;
    }

    private void swapCommon(int rowA, int colA, int rowB, int colB){
        ItemStack temp = common[rowA][colA];
        common[rowA][colA] = common[rowB][colB];
        common[rowB][colB] = temp;
    }

    private int getFirstAvailableCommonIndex(){
        for(int r = 0; r < ROWS; r++){
            for (int c = 0; c < COLUMNS; c++){
                if (common[r][c] == null){
                    return r * ROWS + c;
                }
            }
        }
        return -1;
    }
    /*
    1 2 3 4 5
    6 7 8 9 10
    11 12 13 14 15

     */

    public boolean addCommon(ItemStack itemStack){
        int pos = getFirstAvailableCommonIndex();
        if (pos == -1) return false;
        setCommon(getFirstAvailableCommonIndex(), itemStack);
        return true;
    }

    public boolean shiftEquipItem(UIInventorySlot clicked) {
//        if(player.isInCombat) return false;
//        if (clicked.getAccepts() == ItemCategory.EquipSlot.COMMON_SLOT){
//            if(addCommon(clicked.getItem())) {
//                unEquip(clicked.getAccepts());
//                return true;
//            }
//        }
//        for(UIInventorySlot s : clicked.getInventorySlot(clicked.getAccepts())){
//
//        }
//        ItemInstance equipped = getEquip(clicked.getAccepts());
//        swapItems(clicked,);
        return false;
    }

    public boolean swapItems(UIInventorySlot a, UIInventorySlot b) {
        if (a.getItem() == null){ // It does not make sense to drag Null to somewhere else.
            return false;
        }
        if (!a.canAccept(b.getItem()) || !b.canAccept(a.getItem())){
            return false;
        }
        // Cannot move equipped items without having a common slot.
        if (a.getAccepts() != ItemCategory.EquipSlot.COMMON_SLOT &&
                b.getAccepts() != ItemCategory.EquipSlot.COMMON_SLOT){
            return false;
        }
        if (a.getAccepts() == ItemCategory.EquipSlot.COMMON_SLOT &&
                b.getAccepts() == ItemCategory.EquipSlot.COMMON_SLOT){
            swapCommon(a.getIndex(), b.getIndex());
            return true;
        }

//        if(player.isInCombat) return false;
        /*
            Swap EQUIPPED chestplate with NOT EQUIPPED trinket.
         */

        ItemStack aI = a.getItem();
        ItemCategory.EquipSlot bE = b.getAccepts();
        int aP = a.getIndex();

        if(aP == -1){
            unEquip(a.getAccepts());
            setCommon(b.getIndex(), aI);
            return true;
        }
        equipItem(bE, aI, aP);
        return true;
    }

    public void equipItem(ItemCategory.EquipSlot s, ItemStack i, int iPos){
        ItemStack removeEquipped = unEquip(s);
        setEquip(s, i);
        setCommon(iPos, removeEquipped);
    }

    public Set<ItemStack> getCraftingBagContents() {
        return craftingBagContents;
    }

    public ItemStack getCraftingBag() {
        return craftingBag;
    }

    public ItemStack getCurrency() {
        return currency;
    }

    public ItemStack[][] getCommon() {
        return common;
    }

    public EnumMap<ItemCategory.EquipSlot, ItemStack> getEquipment() {
        return equipment;
    }
}

