package me.brzeph.infra.repository;

import me.brzeph.core.domain.entity.GameEntity;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.item.DroppedItem;

import java.util.HashMap;
import java.util.Map;

public class GameEntityRepository {
    /*
    Padrão de registro:
    [classe]_[contador].
    exemplo:
        Player_001
        Player_002
        Monster_054
        NPC_38
     */

    private static final Map<String, GameEntity> entities = new HashMap<>();
    private static long lastUser = 0;
    private static long lastMonster = 0;
    private static long lastNPC = 0;
    private static long lastItemStack = 0;

    private static String constructEntityIdString(GameEntity entity) {
        /*
        Classes que extender Monster não serão aceitas dinamicamente aqui, isso é intencional.
        É necessário colocar a classe aqui.
         */
        switch (entity){
            case Player pl -> {
                lastUser++;
                return "PLAYER_".concat(String.valueOf(lastUser));
            }
            case Monster ms -> { // Também implementar classes que extends Monster.
                lastMonster++;
                return "MONSTER_".concat(String.valueOf(lastMonster));
            }
            case DroppedItem droppedItem -> {
                lastItemStack++;
                return "ITEM_".concat(String.valueOf(lastItemStack));
            }
            default -> throw new RuntimeException("Unhandled entity type: " + entity.getClass().getName());
        }
    }

    public static String register(GameEntity entity) {
        String id = constructEntityIdString(entity);
        entities.put(id, entity);
        return id;
    }

    public static GameEntity findById(String id) {
        return entities.get(id);
    }

    public static void remove(GameEntity entity) {
        entities.remove(entity.getId());
    }
}

