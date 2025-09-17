package me.brzeph.app.systems.impl;

import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.collisionSystem.events.DropConsumedEvent;
import me.brzeph.core.domain.entity.item.DroppedItem;
import me.brzeph.core.domain.entity.item.ItemCategory;
import me.brzeph.core.domain.entity.item.ItemDefinition;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.core.domain.gui.impl.inventory.InventorySystemInt;
import me.brzeph.core.domain.gui.impl.inventory.PlayerInventory;
import me.brzeph.infra.events.EventBus;

public final class InventorySystem extends SystemAbs implements InventorySystemInt {

    private final EventBus bus;

    public InventorySystem() {
        bus = getBus();
    }

    @Override
    public AddResult addItem(Player player, ItemInstance item, boolean autoEquip) {
        if (item == null) return new AddResult(0, null);

        PlayerInventory inv = player.getInventory();
        ItemDefinition def  = item.def();
        int qty = qty(item);

        // 1) Moeda: vira ouro direto
        if (def.category() == ItemCategory.CURRENCY) {
            inv.addGold(qty);
            postGoldChanged(player, inv.goldAmount());
            return new AddResult(qty, null);
        }

        // 2) Materiais de crafting: vão para a bag
        if (def.category() == ItemCategory.CRAFTING_MATERIAL) {
            inv.addToCraftingBag(clone(item)); // pode somar/compactar depois
            postCraftingChanged(player);
            return new AddResult(qty, null);
        }

        // 3) Poções: tenta slots POTION1..3 (empilhar se mesma poção e stackável)
        if (def.category() == ItemCategory.POTION) {
            int added = tryAddToPotionSlots(inv, item);
            setQty(item, qty - added); // sobrou
            // continua tentando no comum
        }

        // 5) Comum: primeiro empilhar, depois vagas vazias
        int addedCommon = tryStackInCommon(inv, item);
        if (addedCommon < qty) {
            setQty(item, qty - addedCommon);
            addedCommon += tryPlaceInEmptyCommon(inv, item);
        }

        int totalAdded = Math.min(qty, addedCommon);
        ItemInstance remainder = (totalAdded == qty) ? null : copyWithQuantity(item, qty - totalAdded);

        return new AddResult(totalAdded, remainder);
    }

    @Override
    public AddResult pickup(Player player, DroppedItem drop) {
        AddResult res = addItem(player, drop.getItemInstance(), false);
        if (res.added() == 0) return res;
        if (res.remainder() == null) {
            // Remove o drop do mundo
            bus.post(new DropConsumedEvent(drop.getId()));
        } else {
            // Atualizar o drop no mundo com qty restante
            setQty(drop.getItemInstance(), qty(res.remainder()));
        }
        return res;
    }

    // ----------------- Regras internas -----------------

    private int tryAddToPotionSlots(PlayerInventory inv, ItemInstance item){
        int toAdd = qty(item);
        int added = 0;

        ItemCategory.EquipSlot[] POTIONS = {
                ItemCategory.EquipSlot.POTION1, ItemCategory.EquipSlot.POTION2, ItemCategory.EquipSlot.POTION3
        };

        // 3.1 empilhar em slots com a mesma poção
        for (var s : POTIONS){
            ItemInstance cur = inv.getEquip(s);
            if (cur != null && sameItem(cur, item) && isStackable(item)) {
                int room = maxStack(item) - qty(cur);
                if (room > 0) {
                    int put = Math.min(room, toAdd);
                    setQty(cur, qty(cur) + put);
                    toAdd -= put; added += put;
                    if (toAdd == 0) return added;
                }
            }
        }
        // 3.2 colocar em slot vazio
        for (var s : POTIONS){
            ItemInstance cur = inv.getEquip(s);
            if (cur == null) {
                ItemInstance placed = copyWithQuantity(item, Math.min(toAdd, maxStack(item)));
                inv.setEquip(s, placed);
                added += qty(placed);
                toAdd -= qty(placed);
                if (toAdd == 0) break;
            }
        }
        // se sobrar, retorna adicionando ao comum no fluxo do addItem
        return added;
    }

    /** Empilha em slots comuns que tenham o mesmo item. */
    private int tryStackInCommon(PlayerInventory inv, ItemInstance item){
        if (!isStackable(item)) return 0;
        int toAdd = qty(item), added = 0;
        for (int i = 0; i < inv.commonCapacity(); i++){
            ItemInstance cur = inv.getCommon(i);
            if (cur != null && sameItem(cur, item)) {
                int room = maxStack(item) - qty(cur);
                if (room > 0) {
                    int put = Math.min(room, toAdd);
                    setQty(cur, qty(cur) + put);
                    toAdd -= put; added += put;
                    if (toAdd == 0) break;
                }
            }
        }
        return added;
    }

    /** Coloca em espaços vazios do comum, criando stacks até o limite. */
    private int tryPlaceInEmptyCommon(PlayerInventory inv, ItemInstance item){
        int toAdd = qty(item), added = 0, ms = Math.max(1, maxStack(item));
        for (int i = 0; i < inv.commonCapacity(); i++){
            if (inv.getCommon(i) == null){
                int put = Math.min(ms, toAdd);
                inv.setCommon(i, copyWithQuantity(item, put));
                toAdd -= put; added += put;
                if (toAdd == 0) break;
            }
        }
        return added;
    }

    // ----------------- Helpers de item/stack -----------------

    private static boolean sameItem(ItemInstance a, ItemInstance b){
        return a.def().id().equals(b.def().id());
    }
    private static boolean isStackable(ItemInstance ii){
        return ii.def().isStackable();
    }
    private static int maxStack(ItemInstance ii){
        return ii.maxStack();
    }
    private static int qty(ItemInstance ii){
        return ii.quantity();
    }
    private static void setQty(ItemInstance ii, int q){
        ii.setQuantity(q);
    }
    private static ItemInstance copyWithQuantity(ItemInstance src, int q){
        ItemInstance c = clone(src);
        setQty(c, q);
        return c;
    }
    private static ItemInstance clone(ItemInstance src){
        try { return (ItemInstance) src.getClass().getMethod("clone").invoke(src); }
        catch (Exception e){ return src; } // fallback (cuidado: aliasing)
    }

    // ----------------- Eventos para a UI reagir -----------------
    private void postGoldChanged(Player p, int gold){
        bus.post(new GoldChangedEvent(p.getId(), gold));
    }

    private void postCraftingChanged(Player p){
        bus.post(new CraftingBagChangedEvent(p.getId()));
    }
    public record GoldChangedEvent(String playerId, int gold) {}
    public record CraftingBagChangedEvent(String playerId) {}

    @Override
    public void update(float tpf) {

    }
}

