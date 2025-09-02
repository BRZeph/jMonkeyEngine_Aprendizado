package me.brzeph.core.domain.entity.enemies.melee.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.melee.MeleeMonster;

import static me.brzeph.infra.constants.EnemiesConstants.GOBLIN_BASE_SPEED;

public class Goblin extends MeleeMonster {
    public Goblin(Vector3f position, Quaternion rotation) {
        super(position, rotation, "Goblin",
                new CharacterStats(1, 50, 0, GOBLIN_BASE_SPEED, 10f, 1.5f),
                1.5f, 50f, 400f,
                MonsterBehaviour.MELEE);
    }
}

