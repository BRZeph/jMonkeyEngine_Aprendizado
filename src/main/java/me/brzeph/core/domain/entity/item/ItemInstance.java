
package me.brzeph.core.domain.entity.item;

import java.util.*;

public final class ItemInstance {
    private final ItemId id;
    private final ItemDefinition def;

    private int quantity;                       // stack atual (>=1)
    private Integer durability;                 // null se não usar durabilidade
    private Integer charges;                    // null se não usar charges
    private long cooldownUntilMs = 0L;          // relógio do servidor/cliente

    public ItemInstance(ItemDefinition def, int quantity) {
        this.id = ItemId.newId();
        this.def = Objects.requireNonNull(def);
        this.quantity = Math.max(1, quantity);

        def.durability().ifPresent(d -> this.durability = d.maxDurability());
        def.use().ifPresent(u -> { if (u.maxCharges() != null) this.charges = u.maxCharges(); });
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

    public ItemId id() { return id; }
    public ItemDefinition def() { return def; }
    public int quantity() { return quantity; }

    public ItemInstance withQuantity(int newQty) {
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
        if (this.def().stack().isEmpty()) throw new RuntimeException("StackSpec is empty");
        return this.def().stack().get().maxStack();
    }
}
