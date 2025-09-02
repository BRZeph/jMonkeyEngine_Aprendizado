package me.brzeph.core.domain.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.infra.repository.GameEntityRepository;

public class Player extends Character {
//    private String accountId;      // id do dono (sessão)
//    private Inventory inventory; // inventário do player
//    private Stats stats;         // atributos secundários calculados (crit chance, etc.)
//    private Guild guild;         // referência à guilda
//    private List<Quest> activeQuests;

    public Player(Vector3f position, Quaternion rotation, String name,
                  CharacterStats stats,
                  float height, float weight, float jumpForce) {
        super(position, rotation, name, stats, height, weight, jumpForce);
    }
}
