package me.brzeph.core.domain.entity.enemies.caster;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.infra.constants.EnemiesConstants;

public abstract class CasterMonster extends Monster {

    private final CasterStats casterStats;

    public CasterMonster(Vector3f position, Quaternion rotation, String name,
                         CharacterStats stats, float height, float weight, float jumpForce,
                         MonsterBehaviour monsterType, CasterStats casterStats) {
        super(position, rotation, name, stats, height, weight, jumpForce, monsterType);
        this.casterStats = casterStats;
    }

    public CasterStats getCasterStats() { return casterStats; }
}

