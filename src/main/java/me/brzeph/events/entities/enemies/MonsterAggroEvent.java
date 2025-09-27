package me.brzeph.events.entities.enemies;

import me.brzeph.domain.entity.player.Player;
import me.brzeph.domain.entity.enemies.Monster;

public record MonsterAggroEvent(Monster monster, Player player) {
}
