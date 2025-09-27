package me.brzeph.app.systems.impl.animationSystem;

public enum AnimationType {
    Idle_Parado("Idle", false),
    Walk_Andando("Walk", true),
    Run_Correndo("Run", true),
    HIT_FREE_HAND("Hit_Free_Hand", false),
    DIE("Die", false);

    public final String type;
    public final boolean looping;

    AnimationType(String type, boolean looping) {
        this.type = type;
        this.looping = looping;
    }

    public String getType() {
        return type;
    }

    public boolean shouldLoop() {
        return looping;
    }
}
