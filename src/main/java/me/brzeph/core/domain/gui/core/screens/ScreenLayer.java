package me.brzeph.core.domain.gui.core.screens;

// ===================== Camada de Plugins / Telas =====================

/** Ordem lógica de camadas. Quanto maior, mais “acima”. */
public enum ScreenLayer {
    BACKGROUND(0),
    WORLD(10),
    HUD(20),
    PANEL(30),
    MODAL(40);

    public final int order;
    ScreenLayer(int order){ this.order = order; }
}

