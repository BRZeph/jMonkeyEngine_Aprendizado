package me.brzeph.domain.gui.core.others;// ===================== Primitivos =====================

public record Rect(float x, float y, float w, float h){
    public boolean contains(float px, float py){
        return px >= x && py >= y && px <= x + w && py <= y + h;
    }
    // cria um retângulo "insetado"
    public static Rect inset(Rect r, float pad){
        float w = Math.max(0, r.w() - 2f*pad);
        float h = Math.max(0, r.h() - 2f*pad);
        return new Rect(r.x() + pad, r.y() + pad, w, h);
    }

    // cantos direito/baixo
    public static float x2(Rect r){ return r.x() + r.w(); }
    public static float y2(Rect r){ return r.y() + r.h(); }

    // “stroke” desenhando 4 retângulos finos (sem raio)
    public static void strokeRect(UIBackend g, Rect r, Color color, float thickness){
        // top
        g.drawRect(new Rect(r.x(), r.y(), r.w(), thickness), color, 0f);
        // bottom
        g.drawRect(new Rect(r.x(), y2(r) - thickness, r.w(), thickness), color, 0f);
        // left
        g.drawRect(new Rect(r.x(), r.y(), thickness, r.h()), color, 0f);
        // right
        g.drawRect(new Rect(x2(r) - thickness, r.y(), thickness, r.h()), color, 0f);
    }
}

