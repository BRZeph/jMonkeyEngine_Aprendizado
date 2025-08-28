package me.brzeph.explore;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.input.ChaseCamera;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.AnalogListener;

public class Main extends SimpleApplication implements ActionListener {

    private BulletAppState physics;
    private Node player;
    private BetterCharacterControl bcc;
    private boolean left, right, up, down;
    private Vector3f walkDir = new Vector3f();
    private float moveSpeed = 4.5f; // m/s
    private TerrainQuad terrain;
    private BitmapText coordsText;
    private boolean showCoords = false;

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Demo JME3 - Personagem andando");
        settings.setResolution(2560, 1080);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Estado de física
        physics = new BulletAppState();
        stateManager.attach(physics);

        // Luzes básicas
        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.3f)));
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        rootNode.addLight(sun);

        // Chão (estático)
        Geometry floor = createFloor(100, 100);
        rootNode.attachChild(floor);
        RigidBodyControl floorPhy = new RigidBodyControl(
                CollisionShapeFactory.createMeshShape(floor), 0f
        );
        floor.addControl(floorPhy);
        physics.getPhysicsSpace().add(floorPhy);

        // Player (cápsula + física de personagem)
        player = new Node("Player");
        // Visual simples (caixa) só para enxergar
        Box bodyBox = new Box(0.3f, 0.9f, 0.3f);
        Geometry bodyGeo = new Geometry("Body", bodyBox);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", ColorRGBA.Orange);
        bodyGeo.setMaterial(m);
        bodyGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        player.attachChild(bodyGeo);

        // Controle de personagem (usa cápsula interna)
        bcc = new BetterCharacterControl(0.4f, 1.8f, 70f); // raio, altura, massa
        bcc.setGravity(new Vector3f(0, -30f, 0));
        bcc.setJumpForce(new Vector3f(0, 9f, 0));
        player.addControl(bcc);

        // Posição inicial
        player.setLocalTranslation(0, 2, 0);

        rootNode.attachChild(player);
        physics.getPhysicsSpace().add(bcc);

        // Câmera seguindo o personagem (3ª pessoa)
        flyCam.setEnabled(false); // desativa a câmera livre padrão
        ChaseCamera chase = new ChaseCamera(cam, player, inputManager);
        chase.setDefaultDistance(6f);
        chase.setMaxDistance(12f);
        chase.setMinDistance(3f);
        chase.setDefaultVerticalRotation(0.2f);
        chase.setRotationSensitivity(3f);
        chase.setLookAtOffset(new Vector3f(0, 1.2f, 0));

        terrain = createTerrain();
        rootNode.attachChild(terrain);

        // Texto de coordenadas (HUD)
        initCoordinates();

        // Inputs
        initKeys();
    }

    private void initCoordinates() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        coordsText = new BitmapText(guiFont, false);
        coordsText.setSize(guiFont.getCharSet().getRenderedSize());
        coordsText.setText(""); // começa vazio
        coordsText.setLocalTranslation(10, cam.getHeight() - 10, 0); // canto superior esquerdo
        coordsText.setCullHint(Spatial.CullHint.Always); // oculto até apertar F3
        guiNode.attachChild(coordsText);
    }

    private TerrainQuad createTerrain() {
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

        terrain = new TerrainQuad("myTerrain", 65, size, heightMap.getHeightMap());

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

    private Geometry createFloor(float xSize, float zSize) {
        Box box = new Box(xSize, 0.2f, zSize);
        Geometry geo = new Geometry("Floor", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.DarkGray);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        geo.setMaterial(mat);
        geo.setLocalTranslation(0, -0.2f, 0);
        geo.setShadowMode(RenderQueue.ShadowMode.Receive);
        return geo;
    }

    private void initKeys() {
        inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up",    new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down",  new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump",  new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("ToggleCoords", new KeyTrigger(KeyInput.KEY_F3));

        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump", "ToggleCoords");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "Left":  left  = isPressed; break;
            case "Right": right = isPressed; break;
            case "Up":    up    = isPressed; break;
            case "Down":  down  = isPressed; break;
            case "Jump":
                if (isPressed) bcc.jump();
                break;
            case "ToggleCoords":
                if (isPressed) {
                    showCoords = !showCoords;
                    coordsText.setCullHint(showCoords ? Spatial.CullHint.Never : Spatial.CullHint.Always);
                }
                break;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Direção baseada na câmera (clássico WASD em 3ª pessoa)
        Vector3f camDir = cam.getDirection().clone();
        camDir.y = 0; camDir.normalizeLocal();
        Vector3f camLeft = cam.getLeft().clone();
        camLeft.y = 0; camLeft.normalizeLocal();

        walkDir.set(0, 0, 0);
        if (up)    walkDir.addLocal(camDir);
        if (down)  walkDir.addLocal(camDir.negate());
        if (left)  walkDir.addLocal(camLeft);
        if (right) walkDir.addLocal(camLeft.negateLocal());
        walkDir.normalizeLocal().multLocal(moveSpeed);

        bcc.setWalkDirection(walkDir);

        // Faz o "player" olhar para onde anda (gira o modelo)
        if (walkDir.lengthSquared() > FastMath.ZERO_TOLERANCE) {
            Vector3f forward = walkDir.clone().normalizeLocal();
            // rotaciona ao redor do eixo Y para olhar na direção do movimento
            float angle = FastMath.atan2(forward.x, forward.z);
            player.setLocalRotation(player.getLocalRotation().fromAngleAxis(angle, Vector3f.UNIT_Y));
        }
        if (showCoords) {
            Vector3f p = player.getWorldTranslation();
            Vector3f c = cam.getLocation();
            coordsText.setText(String.format(
                    "Player: [x=%.2f, y=%.2f, z=%.2f]\nCamera: [x=%.2f, y=%.2f, z=%.2f]",
                    p.x, p.y, p.z, c.x, c.y, c.z
            ));
        }
    }
}
