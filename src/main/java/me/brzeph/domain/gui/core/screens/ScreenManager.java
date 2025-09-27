package me.brzeph.domain.gui.core.screens;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ScreenManager {
    private final Map<String, Screen> byId = new ConcurrentHashMap<>();
    private final ArrayList<Screen> order = new ArrayList<>();

    private final FlyCamBridge fly;

    public ScreenManager(FlyCamBridge flyCamBridge){
        this.fly = flyCamBridge;
    }

    public Optional<Screen> topMostModal(){
        return orderedTopFirst().stream()
                .filter(s -> s.isVisible() && s.isActive() && s.freezeInputBehind())
                .findFirst();
    }

    // ---------- registro ----------
    public void register(Screen s){
        s.onFlagsChanged(this::applyFlyCam);
        byId.put(s.id(), s);
        order.add(s);
        sort();
        applyFlyCam();
    }

    public void close(String id){
        Screen s = byId.get(id);
        if (s != null) order.remove(s);
        applyFlyCam();
    }

    public Screen getById(String id){
        return byId.get(id);
    }

    public Optional<Screen> find(String id){ return Optional.ofNullable(byId.get(id)); }

    public void bringToFront(String id){
        Screen s = byId.get(id);
        if (s == null) return;
        int maxZ = order.stream().filter(o -> o.layer()==s.layer()).mapToInt(Screen::z).max().orElse(0);
        s.setZ(maxZ + 1);
        sort();
    }

    public void setLayer(String id, ScreenLayer layer){
        find(id).ifPresent(s -> { s.setLayer(layer); sort(); });
    }

    private void sort(){
        order.sort(Comparator.comparingInt((Screen s) -> s.layer().order).thenComparingInt(Screen::z));
    }

    // ---------- desenho ----------
    public void drawAll(){ for (Screen s : order){ if (s.isVisible()) s.draw(); } }

    // ---------- topo→fundo ----------
    public List<Screen> orderedTopFirst(){
        ArrayList<Screen> copy = new ArrayList<>(order);
        copy.sort((a,b) -> {
            int c = Integer.compare(b.layer().order, a.layer().order);
            if (c!=0) return c;
            return Integer.compare(b.z(), a.z());
        });
        return copy;
    }

    // ---------- simples: liga/desliga FlyCam com base no "há UI?" ----------
    public void applyFlyCam(){
        boolean anyBlocking = order.stream().anyMatch(s ->
                s.isVisible() && s.isActive() && s.disableFlyCamWhileActive()
        );

        if (anyBlocking){
            fly.setEnabled(false);          // desliga câmera
            fly.setCursorVisible(true);     // mostra mouse
        } else {
            fly.setEnabled(true);           // liga câmera
            fly.setCursorVisible(false);    // esconde mouse
        }
    }
}


