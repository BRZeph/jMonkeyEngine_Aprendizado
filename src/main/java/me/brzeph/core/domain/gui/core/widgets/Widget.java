package me.brzeph.core.domain.gui.core.widgets;

import me.brzeph.core.domain.gui.core.layout.LayoutParams;
import me.brzeph.core.domain.gui.core.others.*;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Widget<T extends Widget<T>> {
    private final String uid = UUID.randomUUID().toString(); // imutável, interno
    private String id = uid;                                 // exposto (pode mudar)
    protected final ArrayList<Widget<?>> children = new ArrayList<>();
    protected Rect bounds = new Rect(0,0,0,0);
    protected String assetPath = null;
    protected String fontKey = "default";
    protected LayoutParams lp = LayoutParams.wrap();

    @SuppressWarnings("unchecked")
    protected T self(){ return (T) this; }

    // Acesso a serviços
    protected FontManager fonts(){ return UICoreServiceLocator.get(FontManager.class); }
    protected UIBackend g(){ return fonts().backend(); }
    protected UIAssets assets(){ return fonts().assets(); }

    // Fluent API tipado
    public T asset(String path){ this.assetPath = path; return self(); }
    public T font(String key){ this.fontKey = key; return self(); }
    public T layout(LayoutParams lp){ this.lp = lp; return self(); }
    public T add(Widget<?> w){ children.add(w); return self(); }

    // Básicos
    public String id(){ return id; }
    public T id(String newId){ this.id = newId; return self(); }
    public Rect bounds(){ return bounds; }
    public void setBounds(Rect r){ this.bounds = r; }
    public LayoutParams lp(){ return lp; }
    public ArrayList<Widget<?>> children(){ return children; }
    public boolean isVisible(){ return true; }

    // Medição/Desenho padrão
    public Size measure(float maxW, float maxH){ return new Size(bounds.w(), bounds.h()); }

    public void draw(){
        if (!isVisible()) return;
        for (var c : children) c.draw();
    }

    // Hit-test em profundidade
    public Widget<?> hit(float x, float y){
        if (!isVisible()) return null;
        for (int i=children.size()-1;i>=0;i--){
            var h = children.get(i).hit(x,y);
            if (h != null) return h;
        }
        return bounds.contains(x,y) ? this : null;
    }
}
