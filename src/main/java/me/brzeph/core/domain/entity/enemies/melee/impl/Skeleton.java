package me.brzeph.core.domain.entity.enemies.melee.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.melee.MeleeMonster;

import static me.brzeph.infra.constants.EnemiesConstants.SKELETON_BASE_SPEED;

public class Skeleton extends MeleeMonster {
    public Skeleton(Vector3f position, Quaternion rotation) {
        super(position, rotation, "Skeleton",
                new CharacterStats(2, 80, 0, SKELETON_BASE_SPEED, 25f, 1.8f),
                1.7f, 60f, 450f,
                MonsterBehaviour.MELEE);
    }
}
