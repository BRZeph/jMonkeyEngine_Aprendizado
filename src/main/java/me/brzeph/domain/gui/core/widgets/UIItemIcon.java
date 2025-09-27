package me.brzeph.domain.gui.core.widgets;

import me.brzeph.domain.entity.item.ItemCategory;
import me.brzeph.domain.entity.item.ItemInstance;
import me.brzeph.domain.gui.core.others.Color;
import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;
import me.brzeph.domain.gui.core.others.UIFont;

import static me.brzeph.domain.gui.core.others.Rect.*;

public final class UIItemIcon extends Widget<UIItemIcon> {
    private ItemInstance item;
    private Object icon;             // cache de imagem
    private float padding = 4f;

    public UIItemIcon item(ItemInstance it){
        this.item = it;
        this.icon = null;
        return this;
    }

    public ItemInstance item(){ return item; }

    private void ensureIcon(){
        String path = (item != null && item.definition() != null) ? item.definition().getIconPath() : null;
        if (icon == null && path != null) {
            icon = assets().image(path).orElse(null);
        }
    }

    @Override
    public Size measure(float maxW, float maxH){
        float s = Math.min(maxW, maxH); // deixa o slot governar o tamanho
        return new Size(s, s);
    }

    @Override
    public void draw(){
        if (!isVisible()) return;
        if (item == null) return;
        ensureIcon();
        Rect bAll = bounds();
        Rect b = inset(bAll, padding); // substituir bAll.inset(padding)

        // fundo do slot (se preferir no UIInventorySlot, pode remover daqui)
        g().drawRect(bAll, Color.rgba(0,0,0,0.35f), 6f);

        if (icon != null){
            g().drawImage(icon, b);
        }

        if (item != null && item.quantity() > 1){
            UIFont f = fonts().get("default");
            String qty = String.valueOf(item.quantity());
            float tx = x2(b) - g().textWidth(qty, f) - 2f;          // substituir b.x2()
            float ty = y2(b) - g().textLineHeight(f) + 2f;          // substituir b.y2()
            g().drawText(qty, tx, ty, f, Color.rgba(1,1,1,0.9f));
        }

        if (item != null){
            Color rare = item.definition().getRarityColor();
            if (rare != null) strokeRect(g(), bAll, rare, 2f);      // substituir g().strokeRect(...)
        }

        super.draw();
    }

}

