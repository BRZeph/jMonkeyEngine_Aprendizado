package me.brzeph.core.domain.gui.core.widgets;

import me.brzeph.core.domain.gui.core.others.Color;
import me.brzeph.core.domain.gui.core.others.Size;
import me.brzeph.core.domain.gui.core.others.UIFont;

public final class UIText extends Widget<UIText> {
    private String text = "";
    private Color color = Color.gray(1f,1f);

    public UIText text(String t){ this.text = t; return this; }
    public UIText color(Color c){ this.color = c; return this; }

    @Override public Size measure(float maxW, float maxH){
        UIFont f = fonts().get(fontKey);
        float w = g().textWidth(text, f);
        float h = g().textLineHeight(f);
        return new Size(w, h);
    }

    @Override public void draw(){
        if (!isVisible()) return;
        UIFont f = fonts().get(fontKey);
        g().drawText(text, bounds().x(), bounds().y(), f, color);
        super.draw();
    }
}
