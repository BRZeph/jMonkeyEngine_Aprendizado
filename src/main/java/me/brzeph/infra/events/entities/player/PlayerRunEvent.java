package me.brzeph.infra.events.entities.player;

public record PlayerRunEvent(String playerId, boolean isPressed) {
}
