package me.brzeph.domain.gui.core.screens;

import me.brzeph.domain.gui.core.others.FontManager;
import me.brzeph.domain.gui.core.others.UICoreServiceLocator;
import me.brzeph.events.EventBus;

/** Contexto utilitário para plugins (acesso a serviços). */
public final class ScreenContext {
    public FontManager fonts(){ return UICoreServiceLocator.get(FontManager.class); }
    public EventBus bus(){ return UICoreServiceLocator.get(EventBus.class); }
}
