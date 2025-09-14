package me.brzeph.core.domain.gui.core.layout;

import me.brzeph.core.domain.gui.core.others.DimMode;
import me.brzeph.core.domain.gui.core.others.Rect;
import me.brzeph.core.domain.gui.core.others.Size;
import me.brzeph.core.domain.gui.core.widgets.Panel;

public final class ColumnLayout implements Layout {
    private final float gap, padding;
    public ColumnLayout(float gap, float padding){ this.gap=gap; this.padding=padding; }

    @Override
    public Size measure(Panel parent, float maxW, float maxH){
        var kids = parent.children();
        float innerW = Math.max(0, maxW - 2*padding);

        float contentW = 0f;
        float contentH = 0f;
        boolean first = true;

        for (var c : kids){
            var lp = c.lp();
            if (lp.absolute) {
                // considerar o retângulo absoluto como parte do tamanho requerido
                contentW = Math.max(contentW, lp.absoluteRect.x() + lp.absoluteRect.w());
                contentH = Math.max(contentH, lp.absoluteRect.y() + lp.absoluteRect.h());
                continue;
            }

            // Para medir, trate FLEX como WRAP (usa medida preferida do filho)
            Size s = c.measure(innerW, Float.POSITIVE_INFINITY);
            float ch = (lp.main == DimMode.FIXED) ? lp.fixedMain : Math.max(1f, s.h());
            float cw = s.w(); // largura preferida do filho

            contentW = Math.max(contentW, cw);
            contentH += ch;
            if (!first) contentH += gap;
            first = false;
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

        // 1ª passada: medir para saber alturas WRAP e largura preferida (para align X)
        float gapsTotal = gap * Math.max(0, kids.size() - 1);
        float fixedTotal = 0f, totalWeight = 0f;

        float[] heights = new float[kids.size()];
        float[] prefW   = new float[kids.size()];

        for (int i=0;i<kids.size();i++){
            var c  = kids.get(i);
            var lp = c.lp();
            if (lp.absolute) continue;

            Size s = c.measure(innerW, Float.POSITIVE_INFINITY);
            prefW[i] = s.w();

            if (lp.main == DimMode.FIXED){
                heights[i] = lp.fixedMain;
                fixedTotal += heights[i];
            } else if (lp.main == DimMode.WRAP){
                heights[i] = Math.max(1f, s.h());
                fixedTotal += heights[i];
            } else { // FLEX
                // altura será definida depois; tratamos como peso
                totalWeight += Math.max(0.0001f, lp.weight);
            }
        }

        float remaining = Math.max(0, innerH - gapsTotal - fixedTotal);

        // 2ª passada: posicionar
        float cy = y;
        for (int i=0;i<kids.size();i++){
            var c  = kids.get(i);
            var lp = c.lp();

            if (lp.absolute){
                c.setBounds(new Rect(pb.x()+lp.absoluteRect.x(), pb.y()+lp.absoluteRect.y(),
                        lp.absoluteRect.w(), lp.absoluteRect.h()));
                continue;
            }

            float ch = heights[i];
            if (lp.main == DimMode.FLEX){
                ch = (Math.max(0.0001f, lp.weight) / totalWeight) * remaining;
            }

            // alinhamento no eixo X (cruzado)
            float cw, cx;
            switch (lp.crossAlign){
                case START -> { cw = Math.min(prefW[i], innerW); cx = x; }
                case MIDDLE -> { cw = Math.min(prefW[i], innerW); cx = x + (innerW - cw) * 0.5f; }
                case END -> { cw = Math.min(prefW[i], innerW); cx = x + innerW - cw; }
                case STRETCH -> { cw = innerW; cx = x; }
                default -> { cw = innerW; cx = x; }
            }

            c.setBounds(new Rect(cx, cy, cw, ch));
            cy += ch + gap;
        }
    }
}


