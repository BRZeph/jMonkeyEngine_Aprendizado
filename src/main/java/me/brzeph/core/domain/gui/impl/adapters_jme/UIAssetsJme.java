package me.brzeph.core.domain.gui.impl.adapters_jme;


import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import me.brzeph.core.domain.gui.core.others.UIAssets;
import me.brzeph.core.domain.gui.core.others.UIFont;

import java.util.Optional;

public class UIAssetsJme implements UIAssets {
    private final AssetManager am;

    public UIAssetsJme(AssetManager am){
        this.am = am;
    }

    @Override
    public Optional<Object> image(String path) {
        try {
            Texture t = am.loadTexture(path);
            if (t instanceof Texture2D t2) return Optional.of(t2);
            return Optional.empty();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UIFont> font(String path) {
        try { am.loadFont(path); return Optional.of(new UIFont(path, 16f)); }
        catch (Exception e){ return Optional.empty(); }
    }
}
