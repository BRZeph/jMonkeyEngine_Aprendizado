package me.brzeph.domain.gui.core.layout;

import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;
import me.brzeph.domain.gui.core.widgets.Panel;

public final class StaticLayout implements Layout {
    @Override
    public Size measure(Panel parent, float maxW, float maxH){
        float w = 0f, h = 0f;
        for (var c : parent.children()){
            var lp = c.lp();
            if (lp.absolute){
                w = Math.max(w, lp.absoluteRect.x() + lp.absoluteRect.w());
                h = Math.max(h, lp.absoluteRect.y() + lp.absoluteRect.h());
            } else {
                Size s = c.measure(maxW, maxH);
                w = Math.max(w, s.w());
                h = Math.max(h, s.h());
            }
        }
        return new Size(Math.min(maxW, w), Math.min(maxH, h));
    }

    @Override
    public void apply(Panel parent, Rect pb){
        for (var c : parent.children()){
            var lp = c.lp();
            if (lp.absolute){
                c.setBounds(new Rect(pb.x()+lp.absoluteRect.x(), pb.y()+lp.absoluteRect.y(),
                        lp.absoluteRect.w(), lp.absoluteRect.h()));
            }
        }
    }
}

