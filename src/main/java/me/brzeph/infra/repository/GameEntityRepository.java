package me.brzeph.infra.repository;

import me.brzeph.core.domain.entity.GameEntity;
import me.brzeph.core.domain.entity.Player;

import java.util.Collection;
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

    private static String constructEntityIdString(GameEntity entity) {
        switch (entity){
            case Player pl -> {
                lastUser++;
                return "PLAYER_".concat(String.valueOf(lastUser));
            }
//            case NPC npc -> {
//                lastNPC++;
//                return "NPC_".concat(String.valueOf(lastNPC));
//            }
            default -> throw new RuntimeException("Unhandled entity type: " + entity.getClass().getName());
        }

    }

    public static boolean register(GameEntity entity) {
        String id = constructEntityIdString(entity);
        entities.put(id, entity);
        entity.setId(id);
        return true; // Leaving space for validations before registering user.
    }

    public static GameEntity findById(String id) {
        return entities.get(id);
    }

    public static void remove(GameEntity entity) {
        entities.remove(entity.getId());
    }
}

