package me.brzeph.infra.events.entities.enemies;

import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.core.domain.entity.enemies.Monster;

public record MonsterAggroEvent(Monster monster, Player player) {
}
