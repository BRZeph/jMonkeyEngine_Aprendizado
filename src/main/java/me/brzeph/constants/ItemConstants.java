package me.brzeph.constants;

import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.item.ItemCategory.*;
import me.brzeph.domain.entity.item.ItemDefId;
import me.brzeph.domain.entity.item.ItemDefinition;

import java.util.function.Function;

public class ItemConstants {

    public static final String COIN_ITEM_ICON_PATH = "assets/textures/gui/coin.png";
    public static final String COIN_ITEM_ID = "currency_coin";
    public static final String COIN_ITEM_PREFAB_KEY = "dropped_coin";

    public static final String MOCK_ITEM_PREFAB_KEY = "mock_item";

    public static final String CRAFTING_BAG_ICON_PATH = "assets/textures/gui/crafting_bag.png";
    public static final String CRAFTING_BAG_ITEM_ID = "crafting_bag";
    public static final String CRAFTING_BAG_ITEM_PREFAB_KEY = "crafting_bag";

    public static final ItemDefinition CRAFTING_BAG =
            ItemDefinition.builder(
                    ItemDefId.of(CRAFTING_BAG_ITEM_ID), "Gold Coin",
                            EquipSlot.CRAFTING_BAG, ItemRarity.COMMON, CRAFTING_BAG_ICON_PATH
                    )
                    .stack(new StackSpec(1, StackRule.NONE))
                    .dropHint(new DropHint(CRAFTING_BAG_ITEM_PREFAB_KEY, 0.0001f, 0.00000001f))
                    .build();

    public static final ItemDefinition COIN_DEF =
            ItemDefinition.builder(
                    ItemDefId.of(COIN_ITEM_ID), "Gold Coin",
                            EquipSlot.CURRENCY, ItemRarity.COMMON, COIN_ITEM_ICON_PATH
                    )
            .stack(new StackSpec(9999, StackRule.ANY_IF_SAME_DEF))
            .dropHint(new DropHint(COIN_ITEM_PREFAB_KEY, 1.0f, 120f))
            .build();

    public static final ItemDefinition MOCK_ITEM_DEF_HELMET = ItemDefinition.builder(
                    ItemDefId.of(COIN_ITEM_ID), "Helmet",
                    EquipSlot.HELMET, ItemRarity.EPIC, "assets/textures/gui/mock_helmet.png")
            .stack(new StackSpec(1, StackRule.ANY_IF_SAME_DEF))
            .dropHint(new DropHint(COIN_ITEM_PREFAB_KEY, 1.0f, 120f))
            .build();

    public static final ItemDefinition MOCK_ITEM_DEF_CHESTPLATE = ItemDefinition.builder(
                    ItemDefId.of(COIN_ITEM_ID), "Helmet",
                    EquipSlot.CHESTPLATE, ItemRarity.COMMON, "assets/textures/gui/mock_chestplate.png")
            .stack(new StackSpec(1, StackRule.ANY_IF_SAME_DEF))
            .dropHint(new DropHint(COIN_ITEM_PREFAB_KEY, 1.0f, 120f))
            .build();

    public static final ItemDefinition MOCK_ITEM_DEF = ItemDefinition.builder(
                    ItemDefId.of(COIN_ITEM_ID), "Gold Coin",
                    EquipSlot.COMMON_SLOT, ItemRarity.COMMON, COIN_ITEM_ICON_PATH)
            .stack(new StackSpec(7, StackRule.ANY_IF_SAME_DEF))
            .dropHint(new DropHint(COIN_ITEM_PREFAB_KEY, 1.0f, 120f))
            .build();

    public static final Function<String, EntityType> PREFAB_RESOLVER = key -> switch (key) {
        case COIN_ITEM_PREFAB_KEY -> EntityType.DROPPED_COIN;
        case CRAFTING_BAG_ITEM_PREFAB_KEY -> null; // Não pode drop.
        default -> EntityType.DEFAULT_ITEM_PICKUP; // fallback do jogo
    };

}
