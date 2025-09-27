package me.brzeph.domain.entity.enemies.melee.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.domain.entity.CharacterStats;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.domain.entity.enemies.melee.MeleeMonster;

public class Skeleton extends MeleeMonster {

    public Skeleton(EntityType type, Vector3f position, Quaternion rotation, String name, CharacterStats stats, MonsterBehaviour behaviour) {
        super(type, position, rotation, name, stats, behaviour);
    }
}
