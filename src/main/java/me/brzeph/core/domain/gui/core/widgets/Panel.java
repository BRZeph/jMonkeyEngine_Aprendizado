package me.brzeph.core.domain.gui.core.widgets;

import me.brzeph.core.domain.gui.core.layout.Layout;
import me.brzeph.core.domain.gui.core.others.Color;
import me.brzeph.core.domain.gui.core.others.Size;

public final class Panel extends Widget<Panel> {
    private Layout layout;
    private Color bg = null;
    private float corner = 8f;

    public Panel layout(Layout l){ this.layout = l; return this; }
    public Panel background(Color c){ this.bg = c; return this; }
    public Panel corner(float r){ this.corner = r; return this; }

    @Override public Size measure(float maxW, float maxH){
        if (layout != null) return layout.measure(this, maxW, maxH);
        // fallback: empilha filhos verticalmente (simples) se não houver layout
        float w = 0f, h = 0f;
        for (var c : children()){
            Size s = c.measure(maxW, maxH);
            w = Math.max(w, s.w());
            h += s.h();
        }
        return new Size(Math.min(maxW, w), Math.min(maxH, h));
    }

    @Override public void draw(){
        if (!isVisible()) return;
        if (bg != null) g().drawRect(bounds(), bg, corner);
        if (layout != null) layout.apply(this, bounds());
        super.draw();
    }
}

