package me.brzeph.core.domain.entity.enemies.ranged.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.ranged.RangedMonster;

import static me.brzeph.infra.constants.EnemiesConstants.ARCHER_BASE_SPEED;

public class Archer extends RangedMonster {
    public Archer(Vector3f position, Quaternion rotation) {
        super(position, rotation, "Archer",
                new CharacterStats(2, 60, 0, ARCHER_BASE_SPEED, 25f, 2f),
                1.7f, 60f, 450f,
                MonsterBehaviour.RANGED,
                15f // alcance do arco
        );
    }
}

