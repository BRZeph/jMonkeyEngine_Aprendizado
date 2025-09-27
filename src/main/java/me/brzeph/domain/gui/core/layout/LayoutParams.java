package me.brzeph.domain.gui.core.layout;

import me.brzeph.domain.gui.core.others.Align;
import me.brzeph.domain.gui.core.others.DimMode;
import me.brzeph.domain.gui.core.others.Rect;

public final class LayoutParams {
    public DimMode main = DimMode.WRAP;
    public float fixedMain = 0f;
    public float weight = 1f;
    public Align crossAlign = Align.STRETCH;

    // para StaticLayout:
    public boolean absolute = false;
    public Rect absoluteRect = new Rect(0,0,0,0);

    public static LayoutParams wrap(){ return new LayoutParams(); }
    public static LayoutParams fixed(float main){ var lp = new LayoutParams(); lp.main= DimMode.FIXED; lp.fixedMain=main; return lp; }
    public static LayoutParams flex(float weight){ var lp = new LayoutParams(); lp.main= DimMode.FLEX; lp.weight=weight; return lp; }
    public LayoutParams cross(Align a){ this.crossAlign = a; return this; }

    public static LayoutParams absolute(float x,float y,float w,float h){
        var lp = new LayoutParams();
        lp.absolute = true; lp.absoluteRect = new Rect(x,y,w,h);
        return lp;
    }
}
