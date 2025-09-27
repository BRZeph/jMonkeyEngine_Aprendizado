package me.brzeph.domain.gui.core.widgets;

import me.brzeph.domain.gui.core.others.Color;
import me.brzeph.domain.gui.core.others.Rect;
import me.brzeph.domain.gui.core.others.Size;

public final class UIGrid extends Widget<UIGrid> {
    private int columns = 4;
    private float cellGap = 4f;

    public UIGrid columns(int c){ this.columns = Math.max(1, c); return this; }
    public UIGrid gap(float g){ this.cellGap = g; return this; }

    @Override public Size measure(float maxW, float maxH){
        if (children().isEmpty()) return new Size(maxW, 0);
        float maxCellH = 0f, maxCellW = 0f;
        for (var c : children()){
            Size s = c.measure(maxW, maxH);
            maxCellW = Math.max(maxCellW, s.w());
            maxCellH = Math.max(maxCellH, s.h());
        }
        int rows = (int)Math.ceil(children().size() / (float)columns);
        float w = columns*maxCellW + (columns-1)*cellGap;
        float h = rows*maxCellH + (rows-1)*cellGap;
        return new Size(Math.min(maxW, w), Math.min(maxH, h));
    }

    @Override public void draw(){
        if (!isVisible()) return;
        int n = children().size();
        if (n > 0){
            float cellW = (bounds().w() - (columns-1)*cellGap) / columns;
            float maxCellH = 0f;
            for (var c : children()){
                Size s = c.measure(cellW, Float.POSITIVE_INFINITY);
                maxCellH = Math.max(maxCellH, s.h());
            }
            int col = 0, row = 0;
            for (int i=0;i<n;i++){
                var c = children().get(i);
                float x = bounds().x() + col*(cellW + cellGap);
                float y = bounds().y() + row*(maxCellH + cellGap);
                c.setBounds(new Rect(x,y,cellW,maxCellH));
                col++;
                if (col >= columns){ col = 0; row++; }
            }
        } else {
            g().drawRect(bounds(), Color.gray(0.15f, 1f), 4f);
        }
        super.draw();
    }
}
