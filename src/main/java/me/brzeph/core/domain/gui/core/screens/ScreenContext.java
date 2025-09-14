package me.brzeph.core.domain.gui.core.screens;

import me.brzeph.core.domain.gui.core.others.FontManager;
import me.brzeph.core.domain.gui.core.others.UICoreServiceLocator;
import me.brzeph.infra.events.EventBus;

/** Contexto utilitário para plugins (acesso a serviços). */
public final class ScreenContext {
    public FontManager fonts(){ return UICoreServiceLocator.get(FontManager.class); }
    public EventBus bus(){ return UICoreServiceLocator.get(EventBus.class); }
}
