package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import me.brzeph.app.SystemsWiring;
import me.brzeph.app.ports.Audio;
import me.brzeph.app.ports.Input;
import me.brzeph.app.ports.Physics;
import me.brzeph.app.ports.Renderer;
import me.brzeph.app.systems.*;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmePhysics;
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
    private final Renderer renderer;
    private final Audio audio;
    private final Input input;
    private final Physics physics;
    private final EventBus bus;

    private final Camera cam;

    private final CombatSystem combatSystem;
    private final QuestSystem questSystem;
    private final TimeSystem timeSystem;
    private final SaveSystem saveSystem;

    // ---- Runtime refs ----
    private BulletAppState bullet;
    private Node root;
    private Node playerNode; // "spatial" do jogador
    private BetterCharacterControl playerControl;
    private MovementSystem movementSystem;

    public GameState(Renderer renderer, Audio audio, Input input, Physics physics, EventBus bus,
                     CombatSystem combatSystem, QuestSystem questSystem,
                     TimeSystem timeSystem, SaveSystem saveSystem, Camera cam) {
        this.renderer = renderer;
        this.audio = audio;
        this.input = input;
        this.physics = physics;
        this.bus = bus;
        this.combatSystem = combatSystem;
        this.questSystem = questSystem;
        this.timeSystem = timeSystem;
        this.saveSystem = saveSystem;
        this.cam = cam;
    }

    public interface Factory { GameState create(); }

    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;
        this.root = sapp.getRootNode();

        // 1) BulletAppState
        this.bullet = new BulletAppState();
        getStateManager().attach(bullet);

        // 2) passar bullet para o adapter de física
        ((JmePhysics) physics).setBullet(bullet);

        // 3) mundo (terreno, luzes, colliders estáticos, etc.)
        WorldLoader.load(sapp.getAssetManager(), root, physics, cam);

        // 4) HUD
        HudState hud = ServiceLocator.get(HudState.class);
        if (hud != null) getStateManager().attach(hud);

        // 5) input mappings (dispara eventos no EventBus)
        input.bindGameplayMappings(bus);

        // 6) registrar Systems no EventBus
        SystemsWiring.register(bus, combatSystem, questSystem, timeSystem, saveSystem);

        // 7) Player: Node + físico
        this.playerNode = new Node("Player");
        root.attachChild(playerNode);

        getStateManager().attach(new CameraSystem(playerNode, sapp.getCamera()));

        // collider do player (raio, altura, massa)
        this.playerControl = new BetterCharacterControl(1.5f, 6f, 80f);
        playerNode.addControl(playerControl);
        bullet.getPhysicsSpace().add(playerControl);

        // posição inicial segura (ligeiramente acima do terreno)
        playerNode.setLocalTranslation(0, 5f, 0);

        // 8) MovementSystem (escuta MoveKeyEvent e aplica walkDirection)
        this.movementSystem = new MovementSystem(bus, playerControl);
        getStateManager().attach(movementSystem);

        // (opcional) travar rotação do player e usar câmera livre/seguindo
        playerControl.setGravity(new Vector3f(0, -10f, 0));
        playerControl.setJumpForce(new Vector3f(0, 20f, 0));

        sapp.getFlyByCamera().setEnabled(false); // Fazer câmera em terceira pessoa
        ChaseCamera chase = new ChaseCamera(sapp.getCamera(), playerNode, sapp.getInputManager());
        chase.setDefaultDistance(10f);
        chase.setLookAtOffset(new Vector3f(0, 1.6f, 0));
        chase.setMaxDistance(20f);
        chase.setMinDistance(3f);
        chase.setRotationSpeed(2.5f);
    }

    @Override protected void onEnable()  { /* pode focar câmera, etc. */ }
    @Override protected void onDisable() { /* pausar audio, etc. */ }

    @Override
    protected void cleanup(Application app) {
        // desmontagem ordenada
        if (movementSystem != null) {
            getStateManager().detach(movementSystem);
            movementSystem = null;
        }
        if (playerControl != null && bullet != null) {
            bullet.getPhysicsSpace().remove(playerControl);
        }
        if (playerNode != null) {
            playerNode.removeFromParent();
            playerNode = null;
        }
        if (bullet != null) {
            getStateManager().detach(bullet);
            bullet = null;
        }
        // HUD é opcional remover aqui se ele for exclusivo da gameplay
        HudState hud = getStateManager().getState(HudState.class);
        if (hud != null) getStateManager().detach(hud);
    }
}