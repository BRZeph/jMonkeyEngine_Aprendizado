package me.brzeph.core.domain.gui.core.widgets;


import me.brzeph.core.domain.gui.core.others.Color;
import me.brzeph.core.domain.gui.core.others.Rect;
import me.brzeph.core.domain.gui.core.others.Size;

/** Linha separadora horizontal. */
public final class UISeparator extends Widget<UISeparator> {
    private float thickness = 1f;
    private Color color = Color.gray(0.25f, 1f);

    public UISeparator thickness(float t){ this.thickness = Math.max(1f, t); return this; }
    public UISeparator color(Color c){ this.color = c; return this; }

    @Override public Size measure(float maxW, float maxH){
        return new Size(maxW, thickness);
    }

    @Override public void draw(){
        if (!isVisible()) return;
        Rect b = bounds();
        g().drawRect(new Rect(b.x(), b.y(), b.w(), thickness), color, 0f);
        super.draw();
    }
}

