package me.brzeph.domain.gui.core.screens;

/** Contrato base dos plugins de tela. */
public interface ScreenPlugin {
    String id();
    Screen build(ScreenContext ctx, ScreenParams params);
}
