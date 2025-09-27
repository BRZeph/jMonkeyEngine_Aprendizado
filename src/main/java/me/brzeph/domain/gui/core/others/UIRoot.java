package me.brzeph.domain.gui.core.others;

import me.brzeph.domain.gui.core.widgets.Panel;
import me.brzeph.domain.gui.core.widgets.Widget;

public final class UIRoot {
    private final Panel root;
    public UIRoot(Panel root){ this.root = root; }
    public Panel root(){ return root; }

    public void setBounds(float x,float y,float w,float h){ root.setBounds(new Rect(x,y,w,h)); }
    public void draw(){ root.draw(); }
    public Widget<?> hit(float x, float y){ return root.hit(x,y); }
}
