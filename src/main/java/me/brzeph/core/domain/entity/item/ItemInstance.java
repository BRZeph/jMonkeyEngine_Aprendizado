
package me.brzeph.core.domain.entity.item;

import java.util.*;

public final class ItemInstance {
    private final ItemId id;
    private final ItemDefinition def;

    private int quantity;                       // stack atual (>=1)
    private Integer durability;                 // null se não usar durabilidade
    private Integer charges;                    // null se não usar charges
    private long cooldownUntilMs = 0L;          // relógio do servidor/cliente
    private boolean bound;                      // já vinculou? (pickup/equip/use)
    private String boundToCharacterId;          // opcional: quem vinculou

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

    public ItemId id() { return id; }
    public ItemDefinition def() { return def; }
    public int quantity() { return quantity; }
    public Optional<Integer> durability() { return Optional.ofNullable(durability); }
    public Optional<Integer> charges() { return Optional.ofNullable(charges); }
    public boolean isOnCooldown(long nowMs) { return nowMs < cooldownUntilMs; }
    public boolean isBound() { return bound; }
    public Optional<String> boundToCharacterId() { return Optional.ofNullable(boundToCharacterId); }

    public ItemInstance withQuantity(int newQty) {
        if (newQty <= 0) {
            throw new IllegalArgumentException("quantity must be >= 1; para remover use inv.set(index, null)");
        }
        if (!def.isStackable() && newQty != 1) {
            throw new IllegalStateException("item não empilhável deve ter quantity=1");
        }
        int capped = Math.min(newQty, maxStack());
        this.quantity = capped;
        return this;
    }
    public int maxStack() {
        if (!def.isStackable()) return 1;
        if (this.def().stack().isEmpty()) throw new RuntimeException("StackSpec is empty");
        return this.def().stack().get().maxStack();
    }

    /** fingerprint de empilhamento conforme a regra da definição */
    public String stackFingerprint() {
        ItemCategory.StackRule rule = def.stack().map(ItemCategory.StackSpec::rule).orElse(ItemCategory.StackRule.ONLY_IF_IDENTICAL_STATE);
        return switch (rule) {
            case ANY_IF_SAME_DEF -> def.id().value();
            case ONLY_IF_PRISTINE -> def.id().value() + "|PRISTINE:" + (isPristine() ? "Y" : "N");
            case ONLY_IF_IDENTICAL_STATE -> def.id().value() +
                    "|DUR=" + (durability == null ? "-" : durability) +
                    "|CHG=" + (charges == null ? "-" : charges) +
                    "|BND=" + bound; // simplificação
        };
    }

    private boolean isPristine() {
        boolean durOk = durability == null || durability.equals(def.durability().map(ItemCategory.DurabilitySpec::maxDurability).orElse(null));
        boolean chgOk = charges == null || charges.equals(def.use().flatMap(u -> Optional.ofNullable(u.maxCharges())).orElse(null));
        boolean bndOk = !bound;
        return durOk && chgOk && bndOk;
    }

    /** tenta adicionar ao stack respeitando maxStack */
    public int addToStack(int amount) {
        if (!def.isStackable()) return amount;
        int max = def.stack().get().maxStack();
        int canAdd = Math.max(0, max - this.quantity);
        int willAdd = Math.min(canAdd, amount);
        this.quantity += willAdd;
        return amount - willAdd; // sobra
    }

    /** divide o stack, retornando nova instância com a quantidade pedida (estado “copiado”) */
    public ItemInstance split(int amount) {
        if (amount <= 0 || amount >= this.quantity) throw new IllegalArgumentException("amount inválido");
        this.quantity -= amount;
        ItemInstance other = shallowClone(amount);
        return other;
    }

    /** clone raso preservando estado que afeta empilhamento */
    private ItemInstance shallowClone(int qty) {
        ItemInstance i = new ItemInstance(this.def, qty);
        i.durability = this.durability;
        i.charges = this.charges;
        i.cooldownUntilMs = this.cooldownUntilMs;
        i.bound = this.bound;
        i.boundToCharacterId = this.boundToCharacterId;
        return i;
    }

    /** marca vínculo conforme regra da definição */
    public void applyBindIfRequired(String characterId, ItemCategory.BindKind trigger) {
        if (bound) return;
        ItemCategory.BindKind rule = def.bindKind();
        if ((rule == ItemCategory.BindKind.BIND_ON_PICKUP && trigger == ItemCategory.BindKind.BIND_ON_PICKUP) ||
                (rule == ItemCategory.BindKind.BIND_ON_EQUIP  && trigger == ItemCategory.BindKind.BIND_ON_EQUIP)  ||
                (rule == ItemCategory.BindKind.BIND_ON_USE    && trigger == ItemCategory.BindKind.BIND_ON_USE)) {
            bound = true;
            boundToCharacterId = characterId;
        }
    }

    /** consome 1 carga (se houver) */
    public boolean consumeCharge() {
        if (charges == null || charges <= 0) return false;
        charges -= 1;
        return true;
    }

    /** aplica dano na durabilidade (se houver) */
    public void damageDurability(int amount) {
        if (durability == null) return;
        durability = Math.max(0, durability - Math.max(0, amount));
    }

    public void setCooldown(long nowMs) {
        int cd = def.use().map(ItemCategory.UseSpec::cooldownMs).orElse(0);
        cooldownUntilMs = nowMs + cd;
    }
}
