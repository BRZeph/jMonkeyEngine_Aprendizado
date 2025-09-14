package me.brzeph.core.domain.gui.core.others;

import java.util.HashMap;

public final class FontManager {
    private final UIBackend backend;
    private final UIAssets assets;
    private final HashMap<String, UIFont> fonts = new HashMap<>();
    private final UIFont fallback;

    public FontManager(UIBackend backend, UIAssets assets){
        this.backend = backend; this.assets = assets;
        this.fallback = new UIFont("fallback", 16f);
        fonts.put("default", fallback);
    }

    public void register(String name, String assetPath, float size){
        UIFont f = assets.font(assetPath).orElse(new UIFont(assetPath, size));
        fonts.put(name, f);
    }

    public UIFont get(String name){ return fonts.getOrDefault(name, fallback); }
    public UIBackend backend(){ return backend; }
    public UIAssets assets(){ return assets; }
}
