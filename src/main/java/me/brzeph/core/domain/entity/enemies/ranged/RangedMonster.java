package me.brzeph.core.domain.entity.enemies.ranged;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;

public abstract class RangedMonster extends Monster {

    private float attackRange; // alcance do projétil

    public RangedMonster(EntityType type, Vector3f position, Quaternion rotation, String name,
                         CharacterStats stats, MonsterBehaviour behaviour, float attackRange) {
        super(type, position, rotation, name, stats, behaviour);
        this.attackRange = attackRange;
    }

    public float getAttackRange() { return attackRange; }
}

