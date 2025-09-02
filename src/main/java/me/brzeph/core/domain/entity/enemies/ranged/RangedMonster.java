package me.brzeph.core.domain.entity.enemies.ranged;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.infra.constants.EnemiesConstants;

public abstract class RangedMonster extends Monster {

    private float attackRange; // alcance do projétil

    public RangedMonster(Vector3f position, Quaternion rotation, String name,
                         CharacterStats stats, float height, float weight, float jumpForce,
                         MonsterBehaviour monsterType, float attackRange) {
        super(position, rotation, name, stats, height, weight, jumpForce, monsterType);
        this.attackRange = attackRange;
    }

    public float getAttackRange() { return attackRange; }
}

