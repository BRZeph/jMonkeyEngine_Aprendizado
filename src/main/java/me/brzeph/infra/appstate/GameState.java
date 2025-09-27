package me.brzeph.infra.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import me.brzeph.app.systems.SystemsWiring;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.*;
import me.brzeph.app.systems.impl.animationSystem.AnimationSystem;
import me.brzeph.app.systems.impl.collisionSystem.handlers.ItemProximitySystem;
import me.brzeph.app.systems.impl.collisionSystem.CollisionSystem;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.app.systems.impl.InventorySystem;
import me.brzeph.app.factory.EntityFactory;
import me.brzeph.app.factory.WorldFactory;
import me.brzeph.events.EventBus;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import static me.brzeph.constants.PhysicsConstants.WORLD_GRAVITY;

public class GameState extends BaseAppState {
    // ---- Core (injeções) ----
    private final EventBus bus;

    // ---- Systems ----
    private PlayerSystem playerSystem;
    private MonsterSystem monsterSystem;
    private CameraSystem cameraSystem;
    private GUISystem defaultGUISystem;
    private ItemSystem itemSystem;
    private InputSystem inputSystem;
    private CollisionSystem collisionSystem;
    private ItemProximitySystem itemProximitySystem;
    private InventorySystem inventorySystem;
    private AnimationSystem animationSystem;
    private EntityFactory entityFactory;

    // ---- Runtime refs ----
    private BulletAppState bullet;
    private Node root;

    public GameState(EventBus bus, BulletAppState bullet) {
        this.bus = bus;
        this.bullet = bullet;
    }
    @Override
    protected void initialize(Application sapp) {
        initCore((SimpleApplication) sapp);
        initDebugSystem(true, sapp);
        initSystems(sapp);
        initWorld((SimpleApplication) sapp); // TODO: create WorldSystem or similar and refactor this.
    }

    @Override
    public void update(float tpf) {
        collisionSystem.pump();
        itemProximitySystem.update(tpf);
        playerSystem.update(tpf);
        cameraSystem.update(tpf);
        monsterSystem.update(tpf);
        itemSystem.update(tpf);
        defaultGUISystem.update(tpf);
    }

    @Override
    protected void onEnable()  {
    }
    @Override
    protected void onDisable() {
    }

    @Override
    protected void cleanup(Application app) {
        clean();
    }

    private void initCore(SimpleApplication sapp) {
        this.root = sapp.getRootNode();
        bullet.getPhysicsSpace().setGravity(WORLD_GRAVITY);
    }

    private void initSystems(Application app) {
        ServiceLocator.put(BulletAppState.class, bullet);
        ServiceLocator.put(EntityPhysicsAdapter.class, new EntityPhysicsAdapter(bullet));
        ServiceLocator.put(Node.class, root);
        ServiceLocator.put(EventBus.class, bus);
        ServiceLocator.put(SimpleApplication.class, (SimpleApplication) app);
        ServiceLocator.put(AssetManager.class, app.getAssetManager());
        ServiceLocator.put(PhysicsSpace.class, bullet.getPhysicsSpace());
        ServiceLocator.put(EntityFactory.class, new EntityFactory(app.getAssetManager(), bullet.getPhysicsSpace()));

        animationSystem = new AnimationSystem();
        inventorySystem = new InventorySystem();
        collisionSystem = new CollisionSystem();
        defaultGUISystem = new GUISystem();
        playerSystem = new PlayerSystem();
        cameraSystem = new CameraSystem();
        monsterSystem = new MonsterSystem();
        itemSystem = new ItemSystem();
        inputSystem = new InputSystem();
        itemProximitySystem = new ItemProximitySystem();

        SystemsWiring.wireSystems();

        animationSystem.initialize();
        itemSystem.initialize(); // Chamar apenas depois do wireSystems.
        playerSystem.initialize();
        cameraSystem.initialize();
        defaultGUISystem.initialize();
        inputSystem.initialize();
    }

    private void initWorld(SimpleApplication sapp) {
        initLighting();
        WorldFactory.loadFlatWorld(
                sapp.getAssetManager(),
                root,
                bullet
        );
        WorldFactory.createTerrain(
                bullet,
                root,
                sapp.getAssetManager()
        );
    }

    private void initLighting() { // Futuramente substituir por Tone mapping/HDR e corrigir os materiais para tal.
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        root.addLight(sun);

        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.White.mult(0.3f));
        root.addLight(amb);
    }

    private void clean() {
        SystemAbs.cleanUp();
        if (bullet != null) {
            getStateManager().detach(bullet);
            bullet = null;
        }
    }

    private void initDebugSystem(boolean debug, Application sapp) {
        if (!debug) return;
        // (opcional) se você quiser manter sua customização de material:
        bullet.setDebugInitListener(debugRoot -> debugRoot.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override public void visit(Geometry g) {
                // manter sem z-test para garantir overlay perfeito (opcional)
                Material m = g.getMaterial();
                m.getAdditionalRenderState().setDepthTest(false);
                m.getAdditionalRenderState().setDepthWrite(false);
                // e pode deixar no bucket padrão; o postView já garante ordem
            }
        }));

        // === AQUI ESTÁ O PULO DO GATO: postView para o debug ===
        var rm  = sapp.getRenderManager();
        var cam = sapp.getCamera();

        // cria um viewport pós-cena usando a MESMA câmera
        var physDbg = rm.createPostView("physics-debug", cam);

        // limpa apenas o DEPTH para não herdar o z-buffer da cena
        physDbg.setClearFlags(false, true, false);

        // manda o Minie desenhar o debug nesse viewport
        bullet.setDebugViewPorts(physDbg);

        bullet.setDebugEnabled(false);
    }
}