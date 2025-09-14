package me.brzeph.core.domain.entity.item;

import me.brzeph.core.domain.gui.core.others.Color;

import java.util.Map;
import java.util.Set;

/** Categorias e raridades básicas (expanda à vontade) */
public enum ItemCategory {
    ARMOR,
    WEAPON,
    TRINKET,
    CURRENCY,
    CRAFTING_MATERIAL,
    POTION;

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

    /** Slots possíveis de equipamento (ajuste aos slots do seu jogo) */
    public enum EquipSlot {
        HEAD, CHEST, LEGGINGS, BOOTS, GAUNTLET, BRACER, // Armor
        TRINKET1, TRINKET2, TRINKET3, TRINKET4, // Trinkets
        MAIN_HAND,
        OFF_HAND,
        POTION1, POTION2, POTION3,
        CURRENCY,
        CRAFTING_BAG,
        COMMON_SLOT
    }

    /** Regras de empilhamento: como duas instâncias podem se juntar num mesmo stack */
    public enum StackRule {
        /** Mesmo ItemDefinition, ignora estado (útil para gemas, moedas, materiais padronizados) */
        ANY_IF_SAME_DEF,
        /** Só empilha se o estado for “novo/inteiro” (cheio de durabilidade/cargas e sem variações) */
        ONLY_IF_PRISTINE,
        /** Estado idêntico (mesmas durabilidade/cargas/enchant/bind/…) */
        ONLY_IF_IDENTICAL_STATE
    }

    /** Vinculação (tradabilidade) */
    public enum BindKind { NONE, BIND_ON_PICKUP, BIND_ON_EQUIP, BIND_ON_USE }

    /** Alvo lógico de uso (o sistema de efeitos cuidará disso depois) */
    public enum UseTargetKind { SELF, UNIT, GROUND, NONE }

    /** Especificação de pilha (stack) */
    public record StackSpec(int maxStack, StackRule rule) {
        public StackSpec {
            if (maxStack < 1) throw new IllegalArgumentException("maxStack >= 1");
        }
    }

    /** Especificação de durabilidade (em alguns jogos “charges” substituem durabilidade) */
    public record DurabilitySpec(int maxDurability, boolean repairable) {
        public DurabilitySpec {
            if (maxDurability <= 0) throw new IllegalArgumentException("maxDurability > 0");
        }
    }

    /** Especificação de uso (não implementa efeito — apenas declara intenção) */
    public record UseSpec(String actionKey, UseTargetKind targetKind, int cooldownMs, Integer maxCharges) {
        public UseSpec {
            if (cooldownMs < 0) throw new IllegalArgumentException("cooldownMs >= 0");
            if (maxCharges != null && maxCharges <= 0) throw new IllegalArgumentException("maxCharges > 0");
        }
    }

    public record EquipSpec(Set<EquipSlot> allowedSlots, boolean twoHanded, Map<String, Float> statModifiers) {
        public EquipSpec {
            allowedSlots = Set.copyOf(allowedSlots);
            statModifiers = Map.copyOf(statModifiers);
            if (allowedSlots.isEmpty()) throw new IllegalArgumentException("allowedSlots cannot be empty");
        }
    }

    /** Dica para o mundo 3D: como representar esse item quando dropado (chave do prefab/modelo) */
    public record DropHint(String worldPrefabKey, float pickupRadius, float lifetimeSeconds) {
        public DropHint {
            if (pickupRadius <= 0) throw new IllegalArgumentException("pickupRadius > 0");
            if (lifetimeSeconds < 0) throw new IllegalArgumentException("lifetimeSeconds >= 0");
        }
    }
}
