package me.brzeph.infra.jme.factory;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import me.brzeph.app.ports.Physics;
import me.brzeph.infra.jme.adapter.JmePhysics;
import me.brzeph.infra.jme.adapter.SceneRegistry;

public final class WorldLoader {

    public static void load(AssetManager assets, Node root, Physics physics, Camera cam) {
        int patchSize = 65;
        int size = 512;

        AbstractHeightMap heightmap = null;
        try {
            heightmap = new HillHeightMap(size, 40, 20, 60);
            heightmap.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TerrainQuad terrain = new TerrainQuad("terrain", patchSize, size + 1, heightmap.getHeightMap());
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 1f, 2f);

        Material mat = new Material(assets, "Common/MatDefs/Terrain/Terrain.j3md");
        Texture tex = assets.loadTexture(new TextureKey("Textures/Terrain/splat/grass.jpg", false));
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("Tex1", tex);
        mat.setFloat("Tex1Scale", 64f);
        terrain.setMaterial(mat);

        TerrainLodControl control = new TerrainLodControl(terrain, cam);
        terrain.addControl(control);

        root.attachChild(terrain);
        ((JmePhysics) physics).addStaticMesh(terrain);
    }
}
