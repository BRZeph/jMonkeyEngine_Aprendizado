package me.brzeph.core.factory;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;

public final class WorldFactory {

    private WorldFactory() {} // utilitário estático

    public static void loadFlatWorld(AssetManager assets, Node root, BulletAppState bullet) {
        Geometry floor = createFloor(100, 100, assets);
        root.attachChild(floor);
        RigidBodyControl floorPhy = new RigidBodyControl(
                CollisionShapeFactory.createMeshShape(floor), 0f
        );
        floor.addControl(floorPhy);
        floorPhy.setUserObject("terrain");
        bullet.getPhysicsSpace().add(floorPhy);
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

    public static TerrainQuad createTerrain(BulletAppState physics, Node root, AssetManager assetManager) {
        TerrainQuad terrain = getTerrainQuad();

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
        terrainPhy.setUserObject("terrain");
        physics.getPhysicsSpace().add(terrainPhy);

        root.attachChild(terrain);

        return terrain;
    }

    private static TerrainQuad getTerrainQuad() {
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
        return terrain;
    }
}

