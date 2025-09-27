package me.brzeph.domain.entity.enemies.caster.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.domain.entity.CharacterStats;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.domain.entity.enemies.caster.CasterMonster;
import me.brzeph.domain.entity.enemies.caster.CasterStats;

public class Witch extends CasterMonster {
    public Witch(EntityType type, Vector3f position, Quaternion rotation, String name, CharacterStats stats, MonsterBehaviour behaviour, CasterStats casterStats) {
        super(type, position, rotation, name, stats, behaviour, casterStats);
    }
}
