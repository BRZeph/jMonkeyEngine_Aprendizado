package me.brzeph.core.constants;

import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.item.ItemCategory;
import me.brzeph.core.domain.entity.item.ItemCategory.*;
import me.brzeph.core.domain.entity.item.ItemDefId;
import me.brzeph.core.domain.entity.item.ItemDefinition;

import java.util.function.Function;

public class ItemConstants {

    public static final String COIN_ITEM_ICON_PATH = "assets/textures/gui/coin.png";
    public static final String COIN_ITEM_ID = "currency_coin";
    public static final String COIN_ITEM_PREFAB_KEY = "dropped_coin";
    public static final String MOCK_ITEM_PREFAB_KEY = "mock_item";

    public static final ItemDefinition COIN_DEF = ItemDefinition.builder(
                    ItemDefId.of(COIN_ITEM_ID), "Gold Coin",
                    ItemCategory.CURRENCY, ItemRarity.COMMON, COIN_ITEM_ICON_PATH)
            .stack(new StackSpec(9999, StackRule.ANY_IF_SAME_DEF))
            .dropHint(new DropHint(COIN_ITEM_PREFAB_KEY, 1.0f, 120f))
            .build();

    public static final ItemDefinition MOCK_ITEM_DEF = ItemDefinition.builder(
                    ItemDefId.of(COIN_ITEM_ID), "Gold Coin",
                    ItemCategory.ARMOR, ItemRarity.COMMON, COIN_ITEM_ICON_PATH)
            .stack(new StackSpec(9999, StackRule.ANY_IF_SAME_DEF))
            .dropHint(new DropHint(COIN_ITEM_PREFAB_KEY, 1.0f, 120f))
            .build();

    public static final Function<String, EntityType> PREFAB_RESOLVER = key -> switch (key) {
        case COIN_ITEM_PREFAB_KEY -> EntityType.DROPPED_COIN;
        case MOCK_ITEM_PREFAB_KEY -> EntityType.DEFAULT_ITEM_PICKUP;
        default -> EntityType.DEFAULT_ITEM_PICKUP; // fallback do jogo
    };

}
