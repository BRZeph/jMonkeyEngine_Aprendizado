package me.brzeph.infra.jme.adapter.utils;


public enum InputAction {
    MOVE_FORWARD("moveForward", Direction.FORWARD),
    MOVE_BACKWARD("moveBackward", Direction.BACKWARD),
    MOVE_LEFT("moveLeft", Direction.LEFT),
    MOVE_RIGHT("moveRight", Direction.RIGHT),
    JUMP("jump", null),
    DASH("dash", null);

    private final String name;
    private final Direction direction;

    InputAction(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    // Enum para as direções para eventos que tem direção.
    public enum Direction {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }
}
