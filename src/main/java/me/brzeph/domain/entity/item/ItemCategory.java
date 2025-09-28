package me.brzeph.domain.entity.item;

import me.brzeph.domain.gui.core.others.Color;

import java.util.List;

import static me.brzeph.domain.entity.item.ItemCategory.EquipSlot.*;

public enum ItemCategory {
    ARMOR(HELMET, CHESTPLATE, LEGGINGS, BOOTS, GAUNTLET, BRACER),
    HANDS(MAIN_HAND, OFF_HAND),
    TRINKET(TRINKET1, TRINKET2, TRINKET3, TRINKET4),
    CURRENCY(EquipSlot.CURRENCY),
    CRAFTING_MATERIAL(CRAFTING_BAG),
    POTION(POTION1, POTION2, POTION3);

    private final List<EquipSlot> slots;

    ItemCategory(EquipSlot... slots) {
        this.slots = List.of(slots);
    }

    public boolean contains(EquipSlot slot){
        return this.slots.contains(slot);
    }

    public List<EquipSlot> getSlots() {
        return slots;
    }

    public enum EquipSlot {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, GAUNTLET, BRACER,
        TRINKET1, TRINKET2, TRINKET3, TRINKET4,
        MAIN_HAND, OFF_HAND,
        POTION1, POTION2, POTION3,

        CRAFTING_BAG,
        CURRENCY,
        COMMON_SLOT;
    }

    public enum ItemRarity {
        COMMON(new Color(230,230,230,1)),
        UNCOMMON(new Color(109,255,109,1)),
        RARE(new Color(55, 248,242, 1)),
        EPIC(new Color(212,38,255,1)),
        LEGENDARY(new Color(194,160,0,1)),
        UNIQUE(new Color(222,0,0,1));

        private final Color color;

        ItemRarity(Color color){
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    /** Regras de empilhamento: como duas instâncias podem se juntar num mesmo stack */
    public enum StackRule {
        /** Mesmo ItemDefinition, ignora estado (útil para gemas, moedas, materiais padronizados) */
        ANY_IF_SAME_DEF,
        /** Só empilha se o estado for “novo/inteiro” (cheio de durabilidade/cargas e sem variações) */
        ONLY_IF_PRISTINE,
        /** Estado idêntico (mesmas durabilidade/cargas/enchant/bind/…) */
        ONLY_IF_IDENTICAL_STATE,
        /** Não pode stack */
        NONE
    }

    /** Especificação de pilha (stack) */
    public record StackSpec(int maxStack, StackRule rule) {
        public StackSpec {
            if (maxStack < 1) throw new IllegalArgumentException("maxStack >= 1");
        }
    }

    /** Dica para o mundo 3D: como representar esse item quando dropado (chave do prefab/modelo) */
    public record DropHint(String worldPrefabKey, float pickupRadius, float lifetimeSeconds) {
        public DropHint {
            if (pickupRadius <= 0) throw new IllegalArgumentException("pickupRadius > 0");
            if (lifetimeSeconds < 0) throw new IllegalArgumentException("lifetimeSeconds >= 0");
        }
    }

    public record EquipSpec(String glbPath){

    }
}
