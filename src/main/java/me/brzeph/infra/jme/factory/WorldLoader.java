package me.brzeph.infra.jme.factory;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import me.brzeph.infra.jme.adapter.physics.JmeWorldPhysics;

public final class WorldLoader {

    private WorldLoader() {} // utilitário estático

    public static void loadFlatWorld(AssetManager assets, Node root, BulletAppState bullet) {
        Geometry floor = createFloor(100, 100, assets);
        root.attachChild(floor);
        RigidBodyControl floorPhy = new RigidBodyControl(
                CollisionShapeFactory.createMeshShape(floor), 0f
        );
        floor.addControl(floorPhy);
        bullet.getPhysicsSpace().add(floorPhy);

        //        // Criar caixa (paralelepípedo)
//        Box ground = new Box(30f, 0.05f, 30f); // metade das dimensões (60, 0.1, 60)
//        Geometry geom = new Geometry("ground", ground);
//
//        // Material simples (pode trocar por textura depois)
//        Material mat = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
//        mat.setBoolean("UseMaterialColors", true);
//        mat.setColor("Diffuse", ColorRGBA.Green);
//        mat.setColor("Ambient", ColorRGBA.Green.mult(0.3f));
//        geom.setMaterial(mat);
//
//        // Colocar no mundo
//        geom.setLocalTranslation(0, -0.05f, 0); // alinhar no eixo Y
//        root.attachChild(geom);
//
//        // Física estática (massa = 0)
//        RigidBodyControl rbc = new RigidBodyControl(0f);
//        geom.addControl(rbc);
//        bullet.getPhysicsSpace().add(rbc);
    }

    private static Geometry createFloor(float xSize, float zSize, AssetManager assets) {
        Box box = new Box(xSize, 0.2f, zSize);
        Geometry geo = new Geometry("Floor", box);
        Material mat = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.DarkGray);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        geo.setMaterial(mat);
        geo.setLocalTranslation(0, 0f, 0);
        geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        return geo;
    }

    public static void load(AssetManager assets, Node root, BulletAppState bullet, Camera cam) {
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

        // LOD para otimizar renderização
        TerrainLodControl control = new TerrainLodControl(terrain, cam);
        terrain.addControl(control);

        // adiciona ao rootNode para aparecer na cena
        root.attachChild(terrain);

        // adiciona física estática (massa = 0)
        CollisionShape shape = CollisionShapeFactory.createMeshShape(terrain);
        RigidBodyControl rbc = new RigidBodyControl(shape, 0f);
        terrain.addControl(rbc);

        bullet.getPhysicsSpace().add(rbc);
    }

    public static TerrainQuad createTerrain(BulletAppState physics, AssetManager assetManager) {
        int size = 513; // 2^n + 1
        AbstractHeightMap heightMap;
        try {
            // hills: quantos morros serão “depositados”
            // min/max radius: raio dos morros (em unidades de mundo)
            // flattening >= 1 (quanto maior, mais “achatado”)
            int hills = 1200;
            float minRadius = 8f;
            float maxRadius = 40f;
            long flattening = 2;

            heightMap = new HillHeightMap(size, hills, minRadius, maxRadius, flattening);
            heightMap.load();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar heightmap: " + e.getMessage(), e);
        }

        TerrainQuad terrain = new TerrainQuad("myTerrain", 65, size, heightMap.getHeightMap());

        // posicione/escale como preferir
        terrain.setLocalTranslation(0, -5, 0);
        terrain.setLocalScale(2f, 0.7f, 2f); // ↑ aumenta/↓ diminui o relevo (eixo Y)

        // material simples (sem depender do test-data)
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.DarkGray);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        terrain.setMaterial(mat);

        // colisão
        RigidBodyControl terrainPhy = new RigidBodyControl(
                CollisionShapeFactory.createMeshShape(terrain), 0f
        );
        terrain.addControl(terrainPhy);
        physics.getPhysicsSpace().add(terrainPhy);

        return terrain;
    }
}

