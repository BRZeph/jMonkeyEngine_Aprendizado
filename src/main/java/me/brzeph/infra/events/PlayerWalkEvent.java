package me.brzeph.infra.events;

import me.brzeph.infra.jme.adapter.utils.InputAction;
import me.brzeph.infra.jme.adapter.utils.InputState;

public class PlayerWalkEvent {
    private final String playerId;
    private final InputAction.Direction direction;
    private final InputState inputState;

    public PlayerWalkEvent(String playerId, InputAction.Direction direction, InputState inputState) {
        this.playerId = playerId;
        this.direction = direction;
        this.inputState = inputState;
    }

    public String getPlayerId() {
        return playerId;
    }

    public InputAction.Direction getDirection() {
        return direction;
    }

    public InputState getInputState() {
        return inputState;
    }
}


