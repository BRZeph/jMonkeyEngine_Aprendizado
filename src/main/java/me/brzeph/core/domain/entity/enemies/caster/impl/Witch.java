package me.brzeph.core.domain.entity.enemies.caster.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.caster.CasterMonster;
import me.brzeph.core.domain.entity.enemies.caster.CasterStats;

import static me.brzeph.infra.constants.EnemiesConstants.WITCH_BASE_SPEED;

public class Witch extends CasterMonster {
    public Witch(Vector3f position, Quaternion rotation) {
        super(position, rotation, "Witch",
                new CharacterStats(4, 50, 120, WITCH_BASE_SPEED, 30f, 2f),
                1.8f, 55f, 400f,
                MonsterBehaviour.CASTER,
                new CasterStats()
        );
    }
}
