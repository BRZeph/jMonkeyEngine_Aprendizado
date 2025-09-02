package me.brzeph.core.domain.entity.enemies.ranged.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.ranged.RangedMonster;

import static me.brzeph.infra.constants.EnemiesConstants.GUNSLINGER_BASE_SPEED;

public class Gunslinger extends RangedMonster {
    public Gunslinger(Vector3f position, Quaternion rotation) {
        super(position, rotation, "Gunslinger",
                new CharacterStats(3, 70, 0, GUNSLINGER_BASE_SPEED, 25f, 2f),
                1.8f, 65f, 500f,
                MonsterBehaviour.RANGED,
                20f // alcance da arma
        );
    }
}
