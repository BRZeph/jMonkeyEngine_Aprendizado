package me.brzeph.core.domain.entity.enemies.caster;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;

public abstract class CasterMonster extends Monster {

    private final CasterStats casterStats;

    public CasterMonster(EntityType type, Vector3f position, Quaternion rotation, String name,
                         CharacterStats stats, MonsterBehaviour behaviour, CasterStats casterStats) {
        super(type, position, rotation, name, stats, behaviour);
        this.casterStats = casterStats;
    }

    public CasterStats getCasterStats() { return casterStats; }
}

