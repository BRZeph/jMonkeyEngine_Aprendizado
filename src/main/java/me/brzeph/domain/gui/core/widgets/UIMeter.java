package me.brzeph.domain.gui.core.widgets;

import me.brzeph.domain.gui.core.others.Color;
import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;
import me.brzeph.domain.gui.core.others.UIFont;

/** Barra de progresso horizontal simples (com rótulo opcional). */
public final class UIMeter extends Widget<UIMeter> {
    private float value = 0f; // 0..1
    private float h = 12f;
    private Color bg = Color.rgba(0f,0f,0f,0.5f);
    private Color fg = Color.rgba(0.1f,0.7f,0.2f,1f);
    private String label = null;

    public UIMeter value(float v){ this.value = Math.max(0f, Math.min(1f, v)); return this; }
    public UIMeter height(float h){ this.h = Math.max(6f, h); return this; }
    public UIMeter colors(Color bg, Color fg){ this.bg = bg; this.fg = fg; return this; }
    public UIMeter label(String s){ this.label = s; return this; }

    @Override public Size measure(float maxW, float maxH){ return new Size(maxW, h); }

    @Override public void draw(){
        if (!isVisible()) return;
        Rect b = bounds();
        // fundo
        g().drawRect(b, bg, 4f);
        // preenchimento
        float fillW = Math.max(0f, Math.min(b.w(), b.w()*value));
        g().drawRect(new Rect(b.x(), b.y(), fillW, b.h()), fg, 4f);

        if (label != null && !label.isEmpty()){
            UIFont f = fonts().get(fontKey);
            float tx = b.x() + (b.w() - g().textWidth(label, f)) * 0.5f;
            float ty = b.y() + (b.h() - g().textLineHeight(f)) * 0.5f;
            g().drawText(label, tx, ty, f, Color.gray(1f,1f));
        }
        super.draw();
    }
}
