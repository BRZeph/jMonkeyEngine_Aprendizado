package me.brzeph.domain.entity.enemies.ranged.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.domain.entity.CharacterStats;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.domain.entity.enemies.ranged.RangedMonster;

public class Gunslinger extends RangedMonster {
    public Gunslinger(EntityType type, Vector3f position, Quaternion rotation, String name, CharacterStats stats, MonsterBehaviour behaviour, float attackRange) {
        super(type, position, rotation, name, stats, behaviour, attackRange);
    }
}
