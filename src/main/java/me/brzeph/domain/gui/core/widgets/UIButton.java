package me.brzeph.domain.gui.core.widgets;

import me.brzeph.domain.gui.core.others.Color;
import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;
import me.brzeph.domain.gui.core.others.UIFont;

public final class UIButton extends Widget<UIButton> {
    private String label = "Button";
    private Color fallbackColor = Color.gray(0.3f, 1f);
    private Object cachedImage = null;

    public UIButton label(String t){ this.label = t; return this; }
    public UIButton fallback(Color c){ this.fallbackColor = c; return this; }

    private void ensureAssetLoaded(){
        if (cachedImage == null && assetPath != null){
            cachedImage = assets().image(assetPath).orElse(null);
        }
    }

    @Override public Size measure(float maxW, float maxH){
        UIFont f = fonts().get(fontKey);
        float w = g().textWidth(label, f) + 20f;
        float h = Math.max(g().textLineHeight(f) + 12f, 28f);
        return new Size(w, h);
    }

    @Override public void draw(){
        if (!isVisible()) return;
        ensureAssetLoaded();
        Rect b = bounds();
        if (cachedImage != null){
            g().drawImage(cachedImage, b);
        } else {
            g().drawRect(b, fallbackColor, 6f);
        }
        UIFont f = fonts().get(fontKey);
        float tx = b.x() + (b.w() - g().textWidth(label, f)) * 0.5f;
        float ty = b.y() + (b.h() - g().textLineHeight(f)) * 0.5f;
        g().drawText(label, tx, ty, f, Color.gray(1f,1f));
        super.draw();
    }
}
