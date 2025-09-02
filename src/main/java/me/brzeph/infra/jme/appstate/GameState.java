package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;
import me.brzeph.app.systems.*;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.behaviour.GameStateContext;
import me.brzeph.core.domain.entity.enemies.melee.impl.Goblin;
import me.brzeph.core.factory.MonsterFactory;
import me.brzeph.core.factory.PlayerFactory;
import me.brzeph.core.service.LocalLoopbackChatTransport;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.entities.enemies.MonsterSpawnEvent;
import me.brzeph.infra.jme.adapter.JmeInput;
import me.brzeph.infra.jme.adapter.audio.MonsterAudioAdapter;
import me.brzeph.infra.jme.adapter.audio.PlayerAudioAdapter;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.jme.adapter.renderer.GUIRenderAdapter;
import me.brzeph.infra.jme.adapter.renderer.MonsterRenderAdapter;
import me.brzeph.infra.jme.adapter.renderer.PlayerRenderAdapter;
import me.brzeph.infra.jme.factory.WorldLoader;

import java.util.List;

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
    private final GameStateContext gameStateContext;

    // ---- Systems ----
    private PlayerSystem playerSystem;
    private MonsterSystem monsterSystem;
    private CameraSystem cameraSystem;
    private GUISystem guiSystem;
    private ChatSystem chatSystem;

    // ---- Runtime refs ----
    private BulletAppState bullet;
    private Node root;
    private TerrainQuad terrain; // Relevo de "morros" ao redor

    public GameState(EventBus bus) {
        this.bus = bus;
        this.bullet = new BulletAppState();
        // Armazenar instâncias utilizadas no GameState para intercomunicação sistemática.
        this.gameStateContext = GameStateContext.get();
        bullet.setDebugEnabled(false);
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;

        initCore(sapp);
        initSystems(sapp);
        initWorld(sapp); // TODO: create WorldSystem or similar and refactor this.
        initInputs(sapp);
    }

    @Override
    public void update(float tpf) {
        chatSystem.update(tpf);
        playerSystem.update(tpf);
        monsterSystem.update(tpf);
        guiSystem.update(tpf);
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

    private void initCore(SimpleApplication sapp) {
        this.root = sapp.getRootNode();
        sapp.getStateManager().attach(bullet);
    }

    private void initInputs(Application app) {
        input = new JmeInput(playerSystem.getPlayer(), app, bus);
        input.bindGameplayMappings();
    }

    private void initSystems(Application app) {
//        // ---- Systems (app layer) ----
        CharacterPhysicsAdapter characterPhysics = new CharacterPhysicsAdapter(bullet);
        ServiceLocator.put(CharacterPhysicsAdapter.class, characterPhysics);
        playerSystem = new PlayerSystem(
                root, bus, characterPhysics,
                new PlayerRenderAdapter(root), new PlayerAudioAdapter(app.getAssetManager()),
                new PlayerFactory(app.getAssetManager(), characterPhysics)
        );
        monsterSystem = new MonsterSystem(
                root, bus, characterPhysics,
                new MonsterRenderAdapter(root), new MonsterAudioAdapter(app.getAssetManager()),
                new MonsterFactory(app.getAssetManager(), characterPhysics)
        );
        cameraSystem = new CameraSystem(
                playerSystem.getPlayerSpatial(),
                (SimpleApplication) app
        );
        guiSystem = new GUISystem(
                bus,
                new GUIRenderAdapter((SimpleApplication) app)
        );
        chatSystem = new ChatSystem(
                bus,
                guiSystem.getUi(),
                playerSystem
        );

        playerSystem.setCam(cameraSystem.getCam());
        getStateManager().attach(cameraSystem);
        // Wire all systems here so that one system can call another.
        // PS: avoid having different systems calling each other since it's a violation of SRP and SOLID.
        // PS2: though it should be avoided, the spaghetti is sometimes acceptable.
        gameStateContext.put(PlayerSystem.class, playerSystem);
        gameStateContext.put(MonsterSystem.class, monsterSystem);
        gameStateContext.put(CameraSystem.class, cameraSystem);
        gameStateContext.put(ChatSystem.class, chatSystem);
        gameStateContext.put(EventBus.class, bus);

        List<Player> playerList = List.of(playerSystem.getPlayer()); // Deixando mais fácil para tornar multiplayer depois.
        gameStateContext.putList(Player.class, playerList);

        MonsterSystem.initMonster(bus); // Eventualmente será substituído por initSpawners()
        // e colocado dentro do MonsterSystem.initialize().
    }

    private void initWorld(SimpleApplication sapp) {
        initLighting();
        WorldLoader.loadFlatWorld(
                sapp.getAssetManager(),
                root,
                bullet
        );
        this.terrain = WorldLoader.createTerrain(
                bullet,
                root,
                sapp.getAssetManager()
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
        if (bullet != null) {
            getStateManager().detach(bullet);
            bullet = null;
        }
        if (cameraSystem != null) {
            getStateManager().detach(cameraSystem);
            cameraSystem = null;
        }
    }
}