package me.brzeph.core.domain.gui.core.screens;

import java.util.HashMap;
import java.util.Map; /** Parâmetros livres para plugins de tela. */
public final class ScreenParams {
    private final Map<String,Object> bag = new HashMap<>();

    public ScreenParams put(String k, Object v){ bag.put(k,v); return this; }

    @SuppressWarnings("unchecked")
    public <T> T get(String k, Class<T> type){ return (T) bag.get(k); }

    public boolean has(String k){ return bag.containsKey(k); }

    public ScreenParams putAll(ScreenParams other){
        if (other != null) this.bag.putAll(other.bag);
        return this;
    }

    public ScreenParams copy(){
        ScreenParams cp = new ScreenParams();
        cp.bag.putAll(this.bag);
        return cp;
    }
}
