package me.brzeph.infra.jme.adapter;

import com.jme3.anim.AnimComposer;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class JmeRender {
    private final Application app;
    private final AssetManager assets;

    public JmeRender(Application app) {
        this.app = app;
        this.assets = app.getAssetManager();
    }

    public void playAnimation(long entityId, String anim, float speed, boolean loop) {
        Spatial s = SceneRegistry.get(entityId);
        if (s == null) return;
        AnimComposer composer = s.getControl(AnimComposer.class);
        if (composer == null) return;
        composer.setCurrentAction(anim);
        composer.setGlobalSpeed(speed);
        // loop é definido na própria action se precisar (padrão do AnimComposer)
    }

    public void spawnFx(String assetPath, Vector3f worldPos) {
        // Pode ser um modelo pronto ou um emissor genérico rápido:
        ParticleEmitter fx = new ParticleEmitter("fx", ParticleMesh.Type.Triangle, 50);
        fx.setLocalTranslation(worldPos);
        // Material default só pra ilustrar (troque por um asset real se quiser)
        Material mat = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        fx.setMaterial(mat);
        app.getRenderManager().getMainView("Default"); // força resolução de classes
        app.getGuiViewPort(); // idem
        ((com.jme3.app.SimpleApplication) app).getRootNode().attachChild(fx);
        fx.emitAllParticles();
    }

    public void setMaterial(long entityId, String matAssetPath) {
        Spatial s = SceneRegistry.get(entityId);
        if (s == null) return;
        Material mat = assets.loadMaterial(matAssetPath);
        s.setMaterial(mat);
    }
}