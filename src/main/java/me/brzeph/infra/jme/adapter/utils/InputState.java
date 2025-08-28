package me.brzeph.infra.jme.adapter.utils;

public enum InputState {
    PRESSED,   // Tecla pressionada
    HOLDING,   // Tecla mantida pressionada
    RELEASED;  // Tecla solta

    // Métodos de conveniência para verificar o estado
    public boolean isPressed() {
        return this == PRESSED;
    }

    public boolean isHolding() {
        return this == HOLDING;
    }

    public boolean isReleased() {
        return this == RELEASED;
    }
}

