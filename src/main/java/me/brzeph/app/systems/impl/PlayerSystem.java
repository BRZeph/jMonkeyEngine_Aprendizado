package me.brzeph.app.systems.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import me.brzeph.app.service.InputService;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.animationSystem.AnimationSystem;
import me.brzeph.app.systems.impl.animationSystem.AnimationType;
import me.brzeph.domain.entity.CharacterStats;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.item.ItemInstance;
import me.brzeph.domain.entity.player.Player;
import me.brzeph.app.service.PlayerService;
import me.brzeph.events.entities.player.PlayerJumpEvent;
import me.brzeph.events.entities.player.PlayerRunEvent;
import me.brzeph.events.entities.player.PlayerWalkEvent;
import me.brzeph.infra.jme.adapter.audio.PlayerAudioAdapter;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.List;

import static me.brzeph.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY;
import static me.brzeph.constants.ItemConstants.*;
import static me.brzeph.constants.PlayerConstants.PLAYER_WALK_SPEED;
import static me.brzeph.constants.PlayerConstants.PLAYER_RUN_SPEED;

public class PlayerSystem extends SystemAbs {
    private final PlayerAudioAdapter playerAudio;
    private final GUISystem defaultGUISystem;
    private final InventorySystem inventorySystem;
    private final AnimationSystem animationSystem;
    private Player player;
    private Vector3f walkDir = new Vector3f();
    private boolean movingForward = false;   // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingBackward = false;  // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingLeft = false;      // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingRight = false;     // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean chatOpen = false;

    private Camera cam;

    public PlayerSystem() {
        defaultGUISystem = getSystem(GUISystem.class);
        playerAudio = new PlayerAudioAdapter(getAssetManager());
        inventorySystem = getSystem(InventorySystem.class);
        animationSystem = getSystem(AnimationSystem.class);
        if (inventorySystem == null){
            throw new RuntimeException("No inventory system found");
        }
    }

    public void initialize() {
        spawnPlayer();
        giveStarterItems();
        animationSystem.switchAnimation(player, AnimationType.Idle_Parado);
    }

    public void update(float tpf) {
        boolean stopMovement = chatOpen || defaultGUISystem.isOpen(PLAYER_INVENTORY);
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

        getEntityPhysicsAdapter().moveCharacter(player, walkDir); // sempre; será ZERO se sem input
    }

    public void onWalkAction(PlayerWalkEvent event) {
        Player player = (Player) GameEntityRepository.findById(event.playerId());
        if (player == null) return;
        boolean state = event.isPressed();
        switch (event.letter()) {
            case Letter_W:
                movingForward = state;
                break;
            case Letter_S:
                movingBackward = state;
                break;
            case Letter_A:
                movingLeft = state;
                break;
            case Letter_D:
                movingRight = state;
                break;
        }

        boolean walking = getSystem(InputSystem.class).beingHeldDown(List.of(
                InputService.InputAction.Letter_W,
                InputService.InputAction.Letter_A,
                InputService.InputAction.Letter_S,
                InputService.InputAction.Letter_D
        ));

        if (walking) {
            if (player.getCurrentAnimation() != AnimationType.Idle_Parado){
                return;
            }
            animationSystem.switchAnimation(player, AnimationType.Walk_Andando);
        } else {
            animationSystem.switchAnimation(player, AnimationType.Idle_Parado);
        }
    }

    public void onJumpAction(PlayerJumpEvent playerJumpEvent) {
        if(player == null) return;
        if (playerJumpEvent.isPressed()){
            if (getEntityPhysicsAdapter().jumpCharacter(player)){
                playerAudio.playSoundAt(player, "cartoon_jump");
            }
        }
    }

    public void onTriggerRunAction(PlayerRunEvent playerRunEvent) {
        if(player.getStats().isRunning()){
            player.getStats().setSpeed(PLAYER_WALK_SPEED);
        } else {
            player.getStats().setSpeed(PLAYER_RUN_SPEED);
        }
    }

    public void spawnPlayer() {
        player = new Player(
                EntityType.PLAYER_DEFAULT,
                new Vector3f(0, 3, 0),
                new Quaternion(0, 0, 0, 1),
                "Player1",
                new CharacterStats(
                        1, 1, 1, PLAYER_WALK_SPEED, 30f, 2f
                )
        );
        getEntityFactory().build(player);
    }

    public void giveStarterItems() {
        inventorySystem.addItem(player, new ItemInstance(COIN_DEF, 20), false);
        inventorySystem.addItem(player, new ItemInstance(MOCK_ITEM_DEF_HELMET, 1), false);
        inventorySystem.addItem(player, new ItemInstance(MOCK_ITEM_DEF_HELMET, 1), false);
        inventorySystem.addItem(player, new ItemInstance(MOCK_ITEM_DEF_CHESTPLATE, 1), false);
        inventorySystem.addItem(player, new ItemInstance(MOCK_ITEM_DEF_CHESTPLATE, 1), false);
//        for (int i = 0; i < 35; i++){
//            inventorySystem.addItem(player, new ItemInstance(MOCK_ITEM_DEF, 1), false);
//        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setCam(Camera cam) {
        this.cam = cam;
    }
}
