package me.brzeph.infra.events.entities.player;

import me.brzeph.infra.jme.adapter.utils.InputAction;
import me.brzeph.infra.jme.adapter.utils.InputState;

public record PlayerWalkEvent(String playerId, InputAction.Direction direction, InputState inputState) {
}


