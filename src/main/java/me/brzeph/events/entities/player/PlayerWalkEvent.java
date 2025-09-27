package me.brzeph.events.entities.player;

import me.brzeph.app.service.InputService;

public record PlayerWalkEvent(String playerId, InputService.InputAction letter, boolean isPressed) {
}


