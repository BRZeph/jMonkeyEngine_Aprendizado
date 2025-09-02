package me.brzeph.infra.events.entities.enemies;

import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;

public record MonsterAggroEvent(Monster monster, Player player) {
}
