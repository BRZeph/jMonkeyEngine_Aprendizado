package me.brzeph.domain.gui.core.layout;

import me.brzeph.domain.gui.core.others.DimMode;
import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;
import me.brzeph.domain.gui.core.widgets.Panel;

public final class RowLayout implements Layout {
    private final float gap, padding;
    public RowLayout(float gap, float padding){ this.gap=gap; this.padding=padding; }

    @Override
    public Size measure(Panel parent, float maxW, float maxH){
        var kids = parent.children();
        float innerH = Math.max(0, maxH - 2*padding);

        float contentW = 0f;
        float contentH = 0f;
        boolean first = true;

        for (var c : kids){
            var lp = c.lp();
            if (lp.absolute){
                contentW = Math.max(contentW, lp.absoluteRect.x() + lp.absoluteRect.w());
                contentH = Math.max(contentH, lp.absoluteRect.y() + lp.absoluteRect.h());
                continue;
            }

            Size s = c.measure(Float.POSITIVE_INFINITY, innerH);
            float cw = (lp.main == DimMode.FIXED) ? lp.fixedMain : Math.max(1f, s.w());
            float ch = s.h();

            contentW += cw;
            if (!first) contentW += gap;
            first = false;

            contentH = Math.max(contentH, ch);
        }

        float totalW = Math.min(maxW, Math.max(0, contentW) + 2*padding);
        float totalH = Math.min(maxH, Math.max(0, contentH) + 2*padding);
        return new Size(totalW, totalH);
    }

    @Override
    public void apply(Panel parent, Rect pb){
        var kids = parent.children();
        float x = pb.x() + padding;
        float y = pb.y() + padding;
        float innerW = Math.max(0, pb.w() - 2*padding);
        float innerH = Math.max(0, pb.h() - 2*padding);

        // 1ª passada: medidas preferidas
        float gapsTotal = gap * Math.max(0, kids.size() - 1);
        float fixedTotal = 0f, totalWeight = 0f;

        float[] widths  = new float[kids.size()];
        float[] prefH   = new float[kids.size()];

        for (int i=0;i<kids.size();i++){
            var c  = kids.get(i);
            var lp = c.lp();
            if (lp.absolute) continue;

            Size s = c.measure(Float.POSITIVE_INFINITY, innerH);
            prefH[i] = s.h();

            if (lp.main == DimMode.FIXED){
                widths[i] = lp.fixedMain;
                fixedTotal += widths[i];
            } else if (lp.main == DimMode.WRAP){
                widths[i] = Math.max(1f, s.w());
                fixedTotal += widths[i];
            } else { // FLEX
                totalWeight += Math.max(0.0001f, lp.weight);
            }
        }

        float remaining = Math.max(0, innerW - gapsTotal - fixedTotal);

        // 2ª passada: posicionar
        float cx = x;
        for (int i=0;i<kids.size();i++){
            var c  = kids.get(i);
            var lp = c.lp();

            if (lp.absolute){
                c.setBounds(new Rect(pb.x()+lp.absoluteRect.x(), pb.y()+lp.absoluteRect.y(),
                        lp.absoluteRect.w(), lp.absoluteRect.h()));
                continue;
            }

            float cw = widths[i];
            if (lp.main == DimMode.FLEX){
                cw = (Math.max(0.0001f, lp.weight) / totalWeight) * remaining;
            }

            // alinhamento cruzado (eixo Y)
            float ch, cy;
            switch (lp.crossAlign){
                case START -> { ch = Math.min(prefH[i], innerH); cy = y; }
                case MIDDLE -> { ch = Math.min(prefH[i], innerH); cy = y + (innerH - ch) * 0.5f; }
                case END -> { ch = Math.min(prefH[i], innerH); cy = y + innerH - ch; }
                case STRETCH -> { ch = innerH; cy = y; }
                default -> { ch = innerH; cy = y; }
            }

            c.setBounds(new Rect(cx, cy, cw, ch));
            cx += cw + gap;
        }
    }
}

