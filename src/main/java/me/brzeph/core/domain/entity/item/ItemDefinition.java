package me.brzeph.core.domain.entity.item;

import me.brzeph.core.domain.entity.item.ItemCategory.*;
import me.brzeph.core.domain.gui.core.others.Color;

import java.util.*;

public final class ItemDefinition {
    /*
        This class is utilized by ItemConstants AND ONLY ItemConstants to create the constant items (or "shapes" for
     items with variable stats) then it can be instantiated via ItemFactory.
     */
    private final ItemDefId id;
    private final String name;
    private final ItemCategory category;
    private final ItemRarity rarity;
    private final float weight;            // para carga/encumbrance
    private final boolean tradable;        // rápido: tradável no mercado?
    private final BindKind bindKind;       // regra de vinculação
    private final StackSpec stack;         // null => não empilhável (maxStack=1)
    private final DurabilitySpec durability; // opcional
    private final UseSpec use;             // opcional
    private final EquipSpec equip;         // opcional
    private final DropHint dropHint;       // opcional
    private final Set<String> tags;        // livre: “gem”, “fire”, “quest-item”, …
    private final String iconPath;         // caminho para o ícone no /resources.

    private ItemDefinition(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.category = b.category;
        this.rarity = b.rarity;
        this.weight = b.weight;
        this.tradable = b.tradable;
        this.bindKind = b.bindKind;
        this.stack = b.stack;
        this.durability = b.durability;
        this.use = b.use;
        this.equip = b.equip;
        this.dropHint = b.dropHint;
        this.tags = Set.copyOf(b.tags);
        this.iconPath = b.iconPath;
    }

    public Color getRarityColor(){
        return rarity.getColor();
    }

    public String getIconPath() { return iconPath; }
    public ItemDefId id() { return id; }
    public String name() { return name; }
    public ItemCategory category() { return category; }
    public ItemCategory.ItemRarity rarity() { return rarity; }
    public float weight() { return weight; }
    public boolean tradable() { return tradable; }
    public ItemCategory.BindKind bindKind() { return bindKind; }
    public Optional<StackSpec> stack() { return Optional.ofNullable(stack); }
    public Optional<ItemCategory.DurabilitySpec> durability() { return Optional.ofNullable(durability); }
    public Optional<ItemCategory.UseSpec> use() { return Optional.ofNullable(use); }
    public Optional<ItemCategory.EquipSpec> equip() { return Optional.ofNullable(equip); }
    public Optional<ItemCategory.DropHint> dropHint() { return Optional.ofNullable(dropHint); }
    public Set<String> tags() { return tags; }

    public boolean isStackable() { return stack != null && stack.maxStack() > 1; }
    public boolean isUsable()     { return use != null; }
    public boolean isEquippable() { return equip != null; }
    public boolean hasDurability(){ return durability != null; }

    public static Builder builder(ItemDefId id, String name, ItemCategory cat, ItemCategory.ItemRarity rarity, String iconPath) {
        return new Builder(id, name, cat, rarity, iconPath);
    }
    public static final class Builder {
        private final ItemDefId id;
        private final String name;
        private final ItemCategory category;
        private final ItemCategory.ItemRarity rarity;
        private float weight = 0f;
        private boolean tradable = true;
        private BindKind bindKind = BindKind.NONE;
        private StackSpec stack;
        private DurabilitySpec durability;
        private UseSpec use;
        private EquipSpec equip;
        private DropHint dropHint;
        private Set<String> tags = new HashSet<>();
        private final String iconPath;

        private Builder(ItemDefId id, String name, ItemCategory cat, ItemRarity rarity, String iconPath) {
            this.id = Objects.requireNonNull(id);
            this.name = Objects.requireNonNull(name);
            this.category = Objects.requireNonNull(cat);
            this.rarity = Objects.requireNonNull(rarity);
            this.iconPath = Objects.requireNonNull(iconPath);
        }
        public Builder weight(float w) { this.weight = w; return this; }
        public Builder tradable(boolean t) { this.tradable = t; return this; }
        public Builder bind(BindKind b) { this.bindKind = b; return this; }
        public Builder stack(StackSpec s) { this.stack = s; return this; }
        public Builder durability(DurabilitySpec d) { this.durability = d; return this; }
        public Builder use(UseSpec u) { this.use = u; return this; }
        public Builder equip(EquipSpec e) { this.equip = e; return this; }
        public Builder dropHint(DropHint d) { this.dropHint = d; return this; }
        public Builder tags(String... ts) { this.tags.addAll(Arrays.asList(ts)); return this; }
        public ItemDefinition build() { return new ItemDefinition(this); }
    }
}
