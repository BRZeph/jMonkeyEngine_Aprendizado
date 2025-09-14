package me.brzeph.core.domain.entity.enemies.melee.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.melee.MeleeMonster;

public class Goblin extends MeleeMonster {
    public Goblin(EntityType type, Vector3f position, Quaternion rotation, String name, CharacterStats stats) {
        super(type, position, rotation, name, stats, MonsterBehaviour.MELEE);
    }
}

