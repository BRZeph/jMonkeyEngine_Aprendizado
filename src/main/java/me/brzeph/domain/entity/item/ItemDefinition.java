package me.brzeph.domain.entity.item;

import me.brzeph.domain.entity.item.ItemCategory.*;
import me.brzeph.domain.gui.core.others.Color;

import java.util.*;

public final class ItemDefinition {
    /*
        This class is utilized by ItemConstants AND ONLY ItemConstants to create the constant items (or "shapes" for
     items with variable stats) then it can be instantiated via ItemFactory.
     */
    private final ItemDefId id;
    private final String name;
    private final String iconPath;         // caminho para o ícone no /resources.
    private final EquipSlot category;
    private final ItemRarity rarity;
    private final StackSpec stack;         // null => não empilhável (maxStack=1)
    private final DropHint dropHint;       // opcional
    /*
        When creating usable items, create UseSpec, example:
        public enum UseTargetKind { SELF, UNIT, GROUND, NONE }
        public record UseSpec(String actionKey, UseTargetKind targetKind, int cooldownMs, Integer maxCharges) {
            public UseSpec {
                if (cooldownMs < 0) throw new IllegalArgumentException("cooldownMs >= 0");
                if (maxCharges != null && maxCharges <= 0) throw new IllegalArgumentException("maxCharges > 0");
            }
        }

        same for durability and other mechanics.
        public record DurabilitySpec(int maxDurability, boolean repairable) {
            public DurabilitySpec {
                if (maxDurability <= 0) throw new IllegalArgumentException("maxDurability > 0");
            }
        }
     */

    private ItemDefinition(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.category = b.equipSlot;
        this.rarity = b.rarity;
        this.stack = b.stack;
        this.dropHint = b.dropHint;
        this.iconPath = b.iconPath;
    }

    public Color getRarityColor(){
        return rarity.getColor();
    }

    public String getIconPath() { return iconPath; }
    public ItemDefId id() { return id; }
    public String name() { return name; }
    public EquipSlot equipSlot() { return category; }
    public Optional<StackSpec> stack() { return Optional.ofNullable(stack); }
    public Optional<ItemCategory.DropHint> dropHint() { return Optional.ofNullable(dropHint); }

    public boolean isStackable() { return stack != null && stack.maxStack() > 1; }

    public static Builder builder(ItemDefId id, String name, EquipSlot cat, ItemCategory.ItemRarity rarity, String iconPath) {
        return new Builder(id, name, cat, rarity, iconPath);
    }
    public static final class Builder {
        private final ItemDefId id;
        private final String name;
        private final String iconPath;
        private final EquipSlot equipSlot;
        private final ItemCategory.ItemRarity rarity;
        private StackSpec stack;
        private DropHint dropHint;

        private Builder(ItemDefId id, String name, EquipSlot equipSlot, ItemRarity rarity, String iconPath) {
            this.id = Objects.requireNonNull(id);
            this.name = Objects.requireNonNull(name);
            this.equipSlot = Objects.requireNonNull(equipSlot);
            this.rarity = Objects.requireNonNull(rarity);
            this.iconPath = Objects.requireNonNull(iconPath);
        }
        public Builder stack(StackSpec s) { this.stack = s; return this; }
        public Builder dropHint(DropHint d) { this.dropHint = d; return this; }
        public ItemDefinition build() { return new ItemDefinition(this); }
    }
}
