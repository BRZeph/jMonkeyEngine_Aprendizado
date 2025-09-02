package me.brzeph.core.domain.entity.enemies;

public enum MonsterBehaviour {
    MELEE(90),
    CASTER(110),
    ANIMALS(130),
    RANGED(150),
    TOWER(180),
    BOSS(360);

    private final float fov;

    MonsterBehaviour(float fov) {
        this.fov = fov;
    }

    public float getFov() {
        return fov;
    }
}
