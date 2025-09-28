package me.brzeph.domain.entity.specs;

import static me.brzeph.constants.CollisionConstants.*;

public enum BodyKind   {
    RIGID(CONTROL_TYPE_RBC),
    STATIC(CONTROL_TYPE_BCC),
    CHARACTER(CONTROL_TYPE_RBC),
    GHOST(CONTROL_TYPE_GC);

    private final String control;

    BodyKind(String control) {
        this.control = control;
    }

    public String getControlString() {
        return control;
    }
}
