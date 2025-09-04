package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;
import me.brzeph.app.systems.System;
import me.brzeph.app.systems.impl.*;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmeAudio;
import me.brzeph.infra.jme.adapter.JmeInput;
import me.brzeph.infra.jme.adapter.JmeRender;
import me.brzeph.infra.jme.adapter.audio.MonsterAudioAdapter;
import me.brzeph.core.factory.WorldFactory;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

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

    // ---- Systems ----
    private PlayerSystem playerSystem;
    private MonsterSystem monsterSystem;
    private CameraSystem cameraSystem;
    private GUISystem guiSystem;
    private ChatSystem chatSystem;
    private ItemSystem itemSystem;

    // ---- Runtime refs ----
    private BulletAppState bullet;
    private Node root;
    private TerrainQuad terrain; // Relevo de "morros" ao redor

    public GameState(EventBus bus) {
        this.bus = bus;
        this.bullet = new BulletAppState();
        bullet.setDebugEnabled(true);
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
        cameraSystem.update(tpf);
        monsterSystem.update(tpf);
        guiSystem.update(tpf);
        itemSystem.update(tpf);
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
        ServiceLocator.put(EntityPhysicsAdapter.class, new EntityPhysicsAdapter(bullet));
        ServiceLocator.put(Node.class, root);
        ServiceLocator.put(EventBus.class, bus);
        ServiceLocator.put(SimpleApplication.class, (SimpleApplication) app);
        ServiceLocator.put(JmeAudio.class, new JmeAudio(app));
        ServiceLocator.put(JmeRender.class, new JmeRender(app));
        ServiceLocator.put(AssetManager.class, app.getAssetManager());

        guiSystem = new GUISystem();
        playerSystem = new PlayerSystem();
        cameraSystem = new CameraSystem(); // Antes de playerSystem.
        chatSystem = new ChatSystem();
        monsterSystem = new MonsterSystem();
        itemSystem = new ItemSystem();
    }

    private void initWorld(SimpleApplication sapp) {
        initLighting();
        WorldFactory.loadFlatWorld(
                sapp.getAssetManager(),
                root,
                bullet
        );
        this.terrain = WorldFactory.createTerrain(
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
        System.cleanUp();
    }
}