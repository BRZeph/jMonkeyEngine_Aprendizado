package me.brzeph.infra.jme.factory;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import me.brzeph.app.ports.Physics;
import me.brzeph.infra.jme.adapter.SceneRegistry;

/**
 * Utilitário para carregar mundo inicial (terreno, iluminação, etc).
 */
public final class WorldLoader {
    private WorldLoader() {}

    public static void load(AssetManager assets, Node root, Physics physics) {
        // carrega a cena
        var scene = assets.loadModel("Scenes/level1.j3o");
        root.attachChild(scene);

        // luz básica
        var sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-1f, -2f, -3f).normalizeLocal());
        root.addLight(sun);

        // registre a cena com um ID lógico e adicione corpo rígido estático (massa 0)
        final long WORLD_ENTITY_ID = 0L;
        SceneRegistry.put(WORLD_ENTITY_ID, scene);
        physics.addRigidBody(WORLD_ENTITY_ID, 0f); // estático
    }
}
