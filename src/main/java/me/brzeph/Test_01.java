package me.brzeph;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

public class Test_01 extends SimpleApplication implements ActionListener {

    private BulletAppState physics;
    private TerrainQuad terrain;
    private BetterCharacterControl playerControl;
    private Node playerNode = new Node("Player");
    private Vector3f walkDirection = new Vector3f();
    private boolean left, right, up, down, jump;
    private boolean showHUD = true;
    private BitmapText hud;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        physics = new BulletAppState();
        stateManager.attach(physics);

        setupLights();
        setupSky();
        setupTerrain();
        setupPlayer();
        setupCamera();
        setupHUD();
        setupInput();
    }

    private void setupLights() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sun);
    }

    private void setupSky() {
        rootNode.attachChild(SkyFactory.createSky(assetManager,
                "Textures/Sky/Bright/BrightSky.dds",
                SkyFactory.EnvMapType.CubeMap));
    }

    private void setupTerrain() {
        int patchSize = 65;
        int size = 512;

        AbstractHeightMap heightmap = null;
        try {
            heightmap = new HillHeightMap(size, 40, 20, 60); // OK!
            heightmap.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        terrain = new TerrainQuad("terrain", patchSize, size + 1, heightmap.getHeightMap());
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 1f, 2f);

        Material mat = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        Texture tex = assetManager.loadTexture(new TextureKey("Textures/Terrain/splat/grass.jpg", false));
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("Tex1", tex);
        mat.setFloat("Tex1Scale", 64f);
        terrain.setMaterial(mat);

        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);

        rootNode.attachChild(terrain);
        physics.getPhysicsSpace().add(terrain);
    }

    private void setupPlayer() {
        playerControl = new BetterCharacterControl(0.5f, 1.8f, 80f);
        playerNode.setLocalTranslation(0, 30, 0);
        playerNode.addControl(playerControl);
        physics.getPhysicsSpace().add(playerControl);
        rootNode.attachChild(playerNode);
    }

    private void setupCamera() {
        ChaseCamera chaseCam = new ChaseCamera(cam, playerNode, inputManager);
        chaseCam.setDefaultDistance(10);
        chaseCam.setMaxDistance(15);
        chaseCam.setMinDistance(5);
    }

    private void setupHUD() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        hud = new BitmapText(guiFont, false);
        hud.setSize(guiFont.getCharSet().getRenderedSize());
        hud.setColor(ColorRGBA.White);
        hud.setLocalTranslation(10, cam.getHeight() - 10, 0);
        guiNode.attachChild(hud);
    }

    private void setupInput() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("HUD", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump", "HUD");
    }

    @Override
    public void simpleUpdate(float tpf) {
        walkDirection.set(0, 0, 0);
        if (left)  walkDirection.addLocal(cam.getLeft().mult(4f));
        if (right) walkDirection.addLocal(cam.getLeft().mult(-4f));
        if (up)    walkDirection.addLocal(cam.getDirection().mult(6f));
        if (down)  walkDirection.addLocal(cam.getDirection().mult(-4f));

        playerControl.setWalkDirection(walkDirection);
        if (jump) {
            playerControl.jump();
            jump = false;
        }

        if (showHUD) {
            Vector3f pos = playerNode.getLocalTranslation();
            hud.setText(String.format("XYZ: [%.1f, %.1f, %.1f]", pos.x, pos.y, pos.z));
        } else {
            hud.setText("");
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {}

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "Left"  -> left = isPressed;
            case "Right" -> right = isPressed;
            case "Up"    -> up = isPressed;
            case "Down"  -> down = isPressed;
            case "Jump"  -> jump = isPressed;
            case "HUD"   -> {
                if (isPressed) showHUD = !showHUD;
            }
        }
    }
}