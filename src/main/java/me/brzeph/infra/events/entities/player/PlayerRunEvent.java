package me.brzeph.infra.events.entities.player;

import me.brzeph.infra.jme.adapter.utils.InputState;

public record PlayerRunEvent(String playerId, InputState inputState) {
}
