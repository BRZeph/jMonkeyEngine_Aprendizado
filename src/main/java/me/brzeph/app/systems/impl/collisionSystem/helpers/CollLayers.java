package me.brzeph.app.systems.impl.collisionSystem.helpers;

public final class CollLayers {
    public static final int WORLD   = 1 << 0; // terreno, estáticos
    public static final int PLAYER  = 1 << 1;
    public static final int MONSTER = 1 << 2;
    public static final int NPC     = 1 << 3;
    public static final int ITEM    = 1 << 4; // DroppedItem
    public static final int SPELL   = 1 << 5; // projéteis/áreas (normalmente GC)
    public static final int SENSOR  = 1 << 6; // volumes temporários, zonas
    public static final int ALL     = 0xFFFF;
    private CollLayers() {}
}

