package me.brzeph.domain.gui.core.screens;

import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.UIRoot;
import me.brzeph.domain.gui.core.widgets.Widget;

/** Tela concreta construída por um plugin. */
public final class Screen {
    private final String id;
    private final UIRoot ui;
    private ScreenLayer layer;
    private boolean visible = true;
    private boolean active = true;
    private boolean freezeInputBehind = false;
    private int z = 0;

    // NOVO: pede para desabilitar o FlyCam enquanto esta tela estiver ativa+visível
    private boolean disableFlyCamWhileActive = false;

    // callback opcional para o manager reagir a mudanças
    private Runnable flagsChangedCb;

    public Screen(String id, UIRoot ui, ScreenLayer layer){
        this.id = id; this.ui = ui; this.layer = layer;
    }

    public String id(){ return id; }
    public UIRoot ui(){ return ui; }
    public ScreenLayer layer(){ return layer; }
    public Screen setLayer(ScreenLayer l){ this.layer = l; return this; }
    public int z(){ return z; }
    public Screen setZ(int z){ this.z = z; return this; }

    public boolean isVisible(){ return visible; }
    public Screen setVisible(boolean v){ this.visible = v; notifyFlagsChanged(); return this; }

    public boolean isActive(){ return active; }
    public Screen setActive(boolean a){ this.active = a; notifyFlagsChanged(); return this; }

    public boolean freezeInputBehind(){ return freezeInputBehind; }
    public Screen setFreezeInputBehind(boolean f){ this.freezeInputBehind = f; notifyFlagsChanged(); return this; }

    // NOVOS getters/setters
    public boolean disableFlyCamWhileActive(){ return disableFlyCamWhileActive; }
    public Screen setDisableFlyCamWhileActive(boolean b){ this.disableFlyCamWhileActive = b; notifyFlagsChanged(); return this; }

    // usado pelo ScreenManager ao registrar
    public Screen onFlagsChanged(Runnable r){ this.flagsChangedCb = r; return this; }
    private void notifyFlagsChanged(){ if (flagsChangedCb != null) flagsChangedCb.run(); }

    public Rect getBounds(){
        return ui.root().bounds();
    }

    public void setBounds(Rect r){ ui.root().setBounds(r); }
    public void draw(){ if (visible) ui.draw(); }

    public Widget<?> hit(float x, float y){
        if (!visible || !active) return null;
        return ui.hit(x, y);
    }
}

