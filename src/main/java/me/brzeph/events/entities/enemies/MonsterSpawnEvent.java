package me.brzeph.events.entities.enemies;

import me.brzeph.domain.entity.enemies.Monster;

import java.util.Objects;

public record MonsterSpawnEvent(Monster monster) {
    /*
    A classe Monster.class já contém as informações de localidade.
     */
}
