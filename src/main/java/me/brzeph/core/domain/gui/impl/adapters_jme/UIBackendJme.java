package me.brzeph.core.domain.gui.impl.adapters_jme;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import me.brzeph.core.domain.gui.core.others.Color;
import me.brzeph.core.domain.gui.core.others.Rect;
import me.brzeph.core.domain.gui.core.others.UIBackend;
import me.brzeph.core.domain.gui.core.others.UIFont;

public class UIBackendJme implements UIBackend {
    private final AssetManager am;
    private final Node uiNode;

    public UIBackendJme(AssetManager am, Node uiNode){ this.am=am; this.uiNode=uiNode; }

    @Override public void drawRect(Rect r, Color color, float cornerRadius) {
        Quad quad = new Quad(Math.max(1f, r.w()), Math.max(1f, r.h()));
        Geometry g = new Geometry("uiRect", quad);
        g.setQueueBucket(RenderQueue.Bucket.Gui);
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", new ColorRGBA(color.r, color.g, color.b, color.a));
        m.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        g.setMaterial(m);
        g.setLocalTranslation(new Vector3f(r.x(), r.y(), 0));
        uiNode.attachChild(g);
    }

    @Override public void drawImage(Object imageHandle, Rect r) {
        if (!(imageHandle instanceof Texture2D tex)) return;
        Quad quad = new Quad(Math.max(1f, r.w()), Math.max(1f, r.h()));
        Geometry g = new Geometry("uiImage", quad);
        g.setQueueBucket(RenderQueue.Bucket.Gui);
        Material m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setTexture("ColorMap", tex);
        m.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        g.setMaterial(m);
        g.setLocalTranslation(new Vector3f(r.x(), r.y(), 0));
        uiNode.attachChild(g);
    }

    @Override public void drawText(String text, float x, float y, UIFont font, Color color) {
        BitmapFont bf = am.loadFont(font.key);
        BitmapText bt = new BitmapText(bf, false);
        bt.setText(text);
        bt.setColor(new ColorRGBA(color.r, color.g, color.b, color.a));
        bt.setLocalTranslation(x, y + bt.getLineHeight(), 0);
        uiNode.attachChild(bt);
    }

    @Override public float textWidth(String text, UIFont font) {
        BitmapFont bf = am.loadFont(font.key);
        return bf.getLineWidth(text);
    }
    @Override public float textLineHeight(UIFont font) {
        BitmapFont bf = am.loadFont(font.key);
        return bf.getCharSet().getRenderedSize();
    }
}
