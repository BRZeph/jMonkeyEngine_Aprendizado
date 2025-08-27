package me.brzeph.infra.jme.adapter.utils;


public enum InputAction {
    MOVE_FORWARD("moveForward"),
    MOVE_BACKWARD("moveBackward"),
    MOVE_LEFT("moveLeft"),
    MOVE_RIGHT("moveRight"),
    JUMP("jump"),
    DASH("dash"),;

    private final String name;

    InputAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
