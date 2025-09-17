package me.brzeph.app.systems.impl.collisionSystem.events;

import me.brzeph.core.domain.entity.GameEntity;

public record ItemProximityExit(GameEntity entity, GameEntity item) {
}
