
package me.brzeph.domain.entity.item;

import java.util.*;

public final class ItemStack {
    private final ItemId id;
    private final ItemDefinition def;

    private int quantity;                       // stack atual (>=1)

    public ItemStack(ItemDefinition def, int quantity) {
        this.id = ItemId.newId();
        this.def = Objects.requireNonNull(def);
        this.quantity = Math.max(0, quantity);
    }

    public boolean isStackable(){
        return def.isStackable();
    }

    public void setQuantity(int quantity) {
        if (!def.isStackable() && quantity != 1) {
            throw new IllegalStateException("item não empilhável deve ter quantity=1");
        }
        if (def.stack().isPresent()) {
            if (quantity > def.stack().get().maxStack()) {
                throw new IllegalArgumentException("New amount: " + quantity + " grater than maxStack(): " + def.stack().get().maxStack());
            }
        }
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) {
        assert quantity + this.quantity < maxStack();
        this.quantity += quantity;
    }

    public ItemId id() { return id; }
    public int quantity() { return quantity; }

    public ItemStack withQuantity(int newQty) {
//        if (newQty <= 0) {
//            throw new IllegalArgumentException("quantity must be >= 1; para remover use inv.set(index, null)");
//        }
        if (!def.isStackable() && newQty != 1) {
            throw new IllegalStateException("item não empilhável deve ter quantity=1");
        }
        this.quantity = Math.min(newQty, maxStack());
        return this;
    }

    public int maxStack() {
        if (!def.isStackable()) return 1;
        if (this.definition().stack().isEmpty()) throw new RuntimeException("StackSpec is empty");
        return this.definition().stack().get().maxStack();
    }

    public ItemDefinition definition() {
        return def;
    }

    @Override
    public String toString() {
        return "ItemInstance{" +
                "id=" + id +
                ", def=" + def +
                ", quantity=" + quantity +
                '}';
    }
}
