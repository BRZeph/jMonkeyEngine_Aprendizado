package me.brzeph.infra.events;

import me.brzeph.infra.jme.adapter.utils.InputAction;
import me.brzeph.infra.jme.adapter.utils.InputState;

public class PlayerJumpEvent {
    private final String playerId;
    private final InputState inputState;

    public PlayerJumpEvent(String playerId, InputState inputState) {
        this.playerId = playerId;
        this.inputState = inputState;
    }

    public String getPlayerId() {
        return playerId;
    }

    public InputState getInputState() {
        return inputState;
    }
}


