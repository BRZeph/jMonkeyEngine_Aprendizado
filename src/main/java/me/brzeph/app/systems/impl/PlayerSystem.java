package me.brzeph.app.systems.impl;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.app.service.PlayerService;
import me.brzeph.core.domain.gui.impl.inventory.InventoryServiceImpl;
import me.brzeph.core.factory.ItemFactory;
import me.brzeph.infra.events.entities.player.PlayerJumpEvent;
import me.brzeph.infra.events.entities.player.PlayerRunEvent;
import me.brzeph.infra.events.entities.player.PlayerWalkEvent;
import me.brzeph.infra.jme.adapter.audio.PlayerAudioAdapter;
import me.brzeph.infra.repository.GameEntityRepository;

import static me.brzeph.core.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY;
import static me.brzeph.core.constants.ItemConstants.COIN_DEF;
import static me.brzeph.core.constants.ItemConstants.MOCK_ITEM_DEF;
import static me.brzeph.core.constants.PlayerConstants.PLAYER_WALK_SPEED;
import static me.brzeph.core.constants.PlayerConstants.PLAYER_RUN_SPEED;

public class PlayerSystem extends SystemAbs {
    private final PlayerAudioAdapter playerAudio;
    private final GUISystem defaultGUISystem;
    private final InventoryServiceImpl inventoryService;
    private Player player;
    private Vector3f walkDir = new Vector3f();
    private boolean movingForward = false;   // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingBackward = false;  // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingLeft = false;      // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean movingRight = false;     // PROBABLY SHOULD REFACTOR THIS INSIDE THE PLAYER.CLASS
    private boolean chatOpen = false;

    private Camera cam;

    public PlayerSystem() {
        defaultGUISystem = (GUISystem) getSystem(GUISystem.class);
        playerAudio = new PlayerAudioAdapter(getAssetManager());
        this.inventoryService = new InventoryServiceImpl(getBus());
        spawnPlayer();
    }

    public void initialize() {
        giveStarterItems(player);
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
        getEntityFactory().build(player, getRoot());
    }

    public void giveStarterItems(Player player) {
        inventoryService.addItem(player, new ItemInstance(COIN_DEF, 25));
        for (int i = 0; i < 10; i++){
            inventoryService.addItem(player, new ItemInstance(MOCK_ITEM_DEF, 2));
        }
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
