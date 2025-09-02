package me.brzeph.infra.jme.adapter.utils;


import com.jme3.input.KeyInput;

public enum InputAction {
    /* ===================== Movement ===================== */
    JUMP("jump", null, KeyInput.KEY_SPACE),
    MOVE_FORWARD("moveForward", Direction.FORWARD, KeyInput.KEY_W),
    MOVE_BACKWARD("moveBackward", Direction.BACKWARD, KeyInput.KEY_S),
    MOVE_LEFT("moveLeft", Direction.LEFT, KeyInput.KEY_A),
    MOVE_RIGHT("moveRight", Direction.RIGHT, KeyInput.KEY_D),
    TRIGGER_RUN("triggerRun", null, KeyInput.KEY_LSHIFT),
    /* ===================== UI ===================== */
    TOGGLE_INVENTORY("toggleInventory", null, KeyInput.KEY_E),
    /* ===================== Chat ===================== */
    TOGGLE_CHAT("toggleChat", null, KeyInput.KEY_RETURN),
    CHAT_PG_UP("chatPGUP", Direction.UP, KeyInput.KEY_PGUP),
    CHAT_PG_DOWN("chatPGDN", Direction.DOWN, KeyInput.KEY_PGDN);

    private final String name;
    private final Direction direction;
    private final int input;
    private int pressedState;

    InputAction(String name, Direction direction, int input) {
        this.name = name;
        this.direction = direction;
        this.input = input;
        pressedState = 2;
        // TODO : change this to use InputState = 2 instead of pressedState
        // Serve para trackear clicks do botão sem considerar o release.
        // press 3 -> apertou, press 0 -> release, press 1 -> apertei (desativado), press 2 -> soltei (desativado)
    }

    public void press(){
        pressedState = ( pressedState + 1 ) % 4;
    }

    // Enum para as direções para eventos que tem direção.
    public enum Direction {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public int getPressedState() {
        return pressedState;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getInput() {
        return input;
    }
}
