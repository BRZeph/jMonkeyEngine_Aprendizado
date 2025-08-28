package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import me.brzeph.app.systems.*;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.service.*;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmeInput;
import me.brzeph.infra.jme.adapter.audio.JmePlayerAudio;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.jme.adapter.physics.JmeWorldPhysics;
import me.brzeph.infra.jme.adapter.renderer.JmePlayerRenderer;
import me.brzeph.infra.jme.factory.WorldLoader;

public class GameState extends BaseAppState {
    /*
        Analogamente falando, GameState é o mesmo que Level1Screen no proj_3.
        Telas que não envolvem diretamente gameplay (como menus) não são administradas aqui, mas sim em outros States.
        HudState é o que vai dar o overlay de dados criando barras de vida e tudo mais.
        HUD: mostrar dados.
        GUI: envolve interação, então existe lógica.

        Sobre GameState, System, Service, Adapters e Domains:
            GameState -> [*]System -> [*]Service.
            GameState -> [*]System -> [*]Adapters (JmeAudio, JmeInput etc).
            GameState: apenas mapeia ‘inputs’ para os sistemas.
            System: chama Service para cálculos e chama Renderers para utilização do JME.
            Service: puro java, renderização e tudo mais fica no System.

            GameState: faz o link do jogo em si, não a parte de telas e sistemas no ServiceLocator, mas sim a lógica do
            gameplay, linkando sistemas ao bus e registrando eventos ao seu respectivo handler, de tal forma que eventos ao
            serem levantados são captados pelo sistema que foi linkado como handler para tal evento, o sistema em questão
            chama o serviço para fazer cálculos e outras ações puramente Java sem JME, além de chamar classes adapters para
            ações de JME como JmeAudio.class e JmeRenderer.class.

            GameState = orquestrador do gameplay →
            Systems = handlers de eventos →
            Services = lógica pura →
            Adapters = efeito no JME.

            Services e Systems vão estar utilizando os domains para manipulação de entidades e tudo mais.
            [Player faz Input] → JmeInput → (gera evento) → EventBus → System → Service → Adapters.
            EventBus redireciona o Event para o EventHandler (System).
     */

    // ---- Core (injeções) ----
    private JmeInput input;
    private final EventBus bus;
    private Camera cam;

    // ---- Systems ----
    private PlayerSystem playerSystem;
    private CombatSystem combatSystem;
    private QuestSystem questSystem;
    private TimeSystem timeSystem;
    private SaveSystem saveSystem;

    // ---- Runtime refs ----
    private BulletAppState bullet;
    private Node root;

    public GameState(EventBus bus) {
        this.bus = bus;
        this.bullet = new BulletAppState();
    }

    @Override
    public void update(float tpf) {
        playerSystem.update(tpf, cam);
        bullet.update(tpf);
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;
        this.root = sapp.getRootNode();
        sapp.getStateManager().attach(bullet);

        initSystems(sapp);
        initPlayer();
        initWorld(sapp);
        initCamera(sapp);
        initInputs(sapp);
    }

    @Override
    protected void onEnable()  {
        /* pode focar câmera, etc. */
    }
    @Override
    protected void onDisable() {
        /* pausar audio, etc. */
    }

    @Override
    protected void cleanup(Application app) {
        clean();
    }

    private void initInputs(Application app) {
        input = new JmeInput(playerSystem.getPlayer(), app, bus);
        input.bindGameplayMappings();
    }

    private void initPlayer() {
        playerSystem.spawnPlayer(
                new Player(
                        new Vector3f(0, 3, 0),
                        new Quaternion(0, 0, 0, 0),
                        "bauticababau, falou o meu amor, bau bau bau",
                        1,
                        1,
                        1,
                        5
                ),
                getApplication().getAssetManager(),
                root
        );
    }

    private void initCamera(SimpleApplication app) {
        this.cam = app.getCamera();
        ServiceLocator.put(Camera.class, cam);
        // Config inicial da câmera (FOV, near/far, aspect ratio, etc.)
        cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);

        app.getFlyByCamera().setEnabled(false); // Fazer câmera em terceira pessoa
        ChaseCamera chase = new ChaseCamera(app.getCamera(), playerSystem.getPlayerSpatial(), app.getInputManager());
        chase.setDefaultDistance(10f);
        chase.setLookAtOffset(new Vector3f(0, 1.6f, 0));
        chase.setMaxDistance(20f);
        chase.setMinDistance(3f);
        chase.setRotationSpeed(2.5f);
    }

    private void initSystems(Application app) {
//        // ---- Core services (puro Java) ----
//        CombatService combatService  = new CombatService();
//        QuestService questService    = new QuestService();
//        LootService lootService      = new LootService();
//        PathService pathService      = new PathService();
//        // ---- Systems (app layer) ----
//        CombatSystem combatSystem = new CombatSystem(combatService, renderer, audio, eventBus);
//        QuestSystem  questSystem  = new QuestSystem(questService, eventBus);
//        TimeSystem   timeSystem   = new TimeSystem(eventBus);
//        SaveSystem   saveSystem   = new SaveSystem(savePort, assets, eventBus);
        playerSystem = new PlayerSystem(
                bus, new CharacterPhysicsAdapter(bullet),
                new JmePlayerRenderer(root), new JmePlayerAudio(app.getAssetManager()),
                app
        );
        combatSystem = new CombatSystem();
        questSystem  = new QuestSystem();
        timeSystem   = new TimeSystem();
        saveSystem   = new SaveSystem();
    }

    private void initWorld(SimpleApplication sapp) {
        initLighting();
        WorldLoader.loadFlatWorld(
                sapp.getAssetManager(),
                root,
                bullet,
                cam
        );
    }

    private void initLighting() {
        root.addLight(new AmbientLight(ColorRGBA.White.mult(0.3f)));
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        root.addLight(sun);
    }

    private void clean() {
        // desmontagem ordenada
        if (bullet != null) {
            getStateManager().detach(bullet);
            bullet = null;
        }
        // HUD é opcional remover aqui se ele for exclusivo da gameplay
        HudState hud = getStateManager().getState(HudState.class);
        if (hud != null) getStateManager().detach(hud);
    }
}