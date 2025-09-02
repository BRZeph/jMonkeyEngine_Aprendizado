package me.brzeph.app.systems;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.factory.PlayerFactory;
import me.brzeph.core.service.PlayerService;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.entities.player.PlayerJumpEvent;
import me.brzeph.infra.events.entities.player.PlayerRunEvent;
import me.brzeph.infra.events.entities.player.PlayerWalkEvent;
import me.brzeph.infra.jme.adapter.audio.PlayerAudioAdapter;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.jme.adapter.renderer.PlayerRenderAdapter;
import me.brzeph.infra.jme.adapter.utils.InputState;
import me.brzeph.infra.repository.GameEntityRepository;

import static me.brzeph.infra.constants.PlayerConstants.PLAYER_WALK_SPEED;
import static me.brzeph.infra.constants.PlayerConstants.PLAYER_RUN_SPEED;

public class PlayerSystem {

    private final EventBus bus;
    private final Node root;
    private final CharacterPhysicsAdapter physicsAdapter;
    private final PlayerRenderAdapter playerRenderer;
    private final PlayerAudioAdapter playerAudio;
    private final PlayerFactory playerFactory;
    private Spatial playerSpatial;
    private Player player;
    private Vector3f walkDir;
    private boolean movingForward = false;   // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingBackward = false;  // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingLeft = false;      // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingRight = false;     // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean chatOpen = false;

    private Camera cam;

    public PlayerSystem(
            Node root,
            EventBus bus,
            CharacterPhysicsAdapter physicsAdapter,
            PlayerRenderAdapter playerRenderer,
            PlayerAudioAdapter playerAudio,
            PlayerFactory playerFactory
    ) {
        this.root = root;
        this.bus = bus;
        this.physicsAdapter = physicsAdapter;
        this.playerRenderer = playerRenderer;
        this.playerAudio = playerAudio;
        walkDir = new Vector3f();
        this.playerFactory = playerFactory;
        initialize();
    }

    private void initialize() {
        bus.subscribe(PlayerWalkEvent.class, this::onWalkAction);
        bus.subscribe(PlayerJumpEvent.class, this::onJumpAction);
        bus.subscribe(PlayerRunEvent.class , this::onTriggerRunAction);
        spawnPlayer();
    }

    private void onWalkAction(PlayerWalkEvent event) {
        Player player = (Player) GameEntityRepository.findById(event.playerId());
        if (player == null) return;
        boolean state = event.inputState() == InputState.PRESSED || event.inputState() == InputState.HOLDING;
        switch (event.direction()) {
            case FORWARD:
                movingForward = state;
                break;
            case BACKWARD:
                movingBackward = state;
                break;
            case LEFT:
                movingLeft = state;
                break;
            case RIGHT:
                movingRight = state;
                break;
        }
    }

    private void onJumpAction(PlayerJumpEvent playerJumpEvent) {
        if(player == null) return;
        switch (playerJumpEvent.inputState()) {
            case PRESSED:
                if(physicsAdapter.jumpCharacter(player)) playerAudio.playSoundAt(player, "cartoon_jump");
                break;

            case RELEASED:
                // Para quando for fazer salto proporcional ao tempo segurado.
                break;
        }
    }

    private void onTriggerRunAction(PlayerRunEvent playerRunEvent) {
        if(player.getStats().isRunning()){
            player.getStats().setSpeed(PLAYER_WALK_SPEED);
        } else {
            player.getStats().setSpeed(PLAYER_RUN_SPEED);
        }
    }

    public void update(float tpf) {
        boolean stopMovement = chatOpen;
        if (stopMovement) {
            movingForward = false;
            movingBackward = false;
            movingLeft = false;
            movingRight = false;
        }
        walkDir = PlayerService.calculateWalkDir(
                cam, player,
                movingForward, movingBackward,
                movingLeft, movingRight
        );
        physicsAdapter.moveCharacter(player, walkDir); // sempre; será ZERO se sem input
    }

    public void spawnPlayer() {
        this.player = new Player(
                new Vector3f(0, 3, 0),
                new Quaternion(0, 0, 0, 1),
                "Player1",
                new CharacterStats(1, 1, 1, PLAYER_WALK_SPEED, 30f, 2f
                ),
                1.8f,
                80f,
                500f
        );
        this.playerSpatial = playerFactory.setupCharacter(player, root);
    }

    public Spatial getPlayerSpatial() {
        return playerSpatial;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCam(Camera cam) {
        this.cam = cam;
    }

    public void inventoryJustToggled(){
        this.chatOpen = !this.chatOpen;
    }
}
