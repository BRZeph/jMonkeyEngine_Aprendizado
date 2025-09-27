package me.brzeph.domain.gui.core.others;

public final class Color {
    public final float r,g,b,a;
    public Color(float r,float g,float b,float a){this.r=r;this.g=g;this.b=b;this.a=a;}
    public static Color rgba(float r,float g,float b,float a){return new Color(r,g,b,a);}
    public static Color gray(float v,float a){return new Color(v,v,v,a);}
}
