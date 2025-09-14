package me.brzeph.core.constants;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public final class PhysicsConstants {
    public static final float G = 9.81f;                      // módulo
    public static final Vector3f WORLD_GRAVITY = new Vector3f(0.0f, -10f, 0.0f);

    // ------------- Renderização da "borda" do personagem primitivo. ------------- //
    public static final ColorRGBA OUTLINE_COLOR = ColorRGBA.Black;
    public static final float OUTLINE_LINE_WIDTH = 2f;     // pode ser ignorado pela GPU
    public static final float OUTLINE_SCALE_BIAS = 1.001f; // p/ evitar z-fighting (descola um tiquinho)
}
