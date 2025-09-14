package me.brzeph.core.domain.entity.enemies.melee;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;

public abstract class MeleeMonster extends Monster {

    public MeleeMonster(EntityType type, Vector3f position, Quaternion rotation, String name,
                        CharacterStats stats, MonsterBehaviour behaviour) {
        super(type, position, rotation, name, stats, behaviour);
    }
}
