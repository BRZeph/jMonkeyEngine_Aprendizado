package me.brzeph.app.systems;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.service.PlayerService;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.PlayerJumpEvent;
import me.brzeph.infra.events.PlayerWalkEvent;
import me.brzeph.infra.jme.adapter.audio.JmePlayerAudio;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.jme.adapter.renderer.JmePlayerRenderer;
import me.brzeph.infra.jme.adapter.utils.InputAction;
import me.brzeph.infra.jme.adapter.utils.InputState;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.HashMap;
import java.util.Map;

public class PlayerSystem {

    private final EventBus bus;
    private final PlayerService playerService;
    private final CharacterPhysicsAdapter physicsAdapter;
    private final JmePlayerRenderer playerRenderer;
    private final JmePlayerAudio playerAudio;
    private Spatial playerSpatial;
    private Node playerNode;
    private Player player;
    private Vector3f walkDir;
    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    public PlayerSystem(EventBus bus,
                        CharacterPhysicsAdapter physicsAdapter,
                        JmePlayerRenderer playerRenderer,
                        JmePlayerAudio playerAudio,
                        Application app) {
        this.bus = bus;
        this.playerService  = new PlayerService();
        this.physicsAdapter = physicsAdapter;
        this.playerRenderer = playerRenderer;
        this.playerAudio = playerAudio;
        initialize(app);
        walkDir = new Vector3f();
    }

    private void initialize(Application app) {
        bus.subscribe(PlayerWalkEvent.class, this::onWalkAction);
        bus.subscribe(PlayerJumpEvent.class, this::onJumpAction);
    }

    public void update(float tpf, Camera cam) {
        /*
        tratar updates dentro dos eventos, se for update sem evento tratar aqui.
         */
        walkDir = playerService.calculateWalkDir(
                cam, player,
                movingForward, movingBackward,
                movingLeft, movingRight
        );
        if(movingForward || movingBackward || movingLeft || movingRight) {
            physicsAdapter.moveCharacter(player, walkDir);
        } else {
            physicsAdapter.moveCharacter(player, new Vector3f(0,0,0));
        }
    }

    private void onJumpAction(PlayerJumpEvent playerJumpEvent) {
        if(player == null) return;
        switch (playerJumpEvent.getInputState()) {
            case PRESSED:
                physicsAdapter.jumpCharacter(player);
//                playerAudio.playJumpSound();
                break;

            case RELEASED:
                // Para quando for fazer salto proporcional ao tempo segurado.
                break;
        }
    }

    private void onWalkAction(PlayerWalkEvent event) {
        Player player = (Player) GameEntityRepository.findById(event.getPlayerId());
        if (player == null) return;
        boolean state = event.getInputState() == InputState.PRESSED || event.getInputState() == InputState.HOLDING;
        switch (event.getDirection()) {
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

    public void spawnPlayer(Player player, AssetManager assetManager, Node root) {
        final float hitBoxSize = player.getHeight()/2;
        /*
        Box cria hitBoxSize para cima e hitBoxSize para baixo, usar metade do tamanho do player.
        Dentro da física usar o tamanho inteiro do player.
         */
        playerNode = new Node(player.getId());
        Box bodyBox = new Box(0.3f, hitBoxSize, 0.3f);
        Geometry bodyGeo = new Geometry("Body", bodyBox);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", ColorRGBA.Orange);
        bodyGeo.setMaterial(m);
        bodyGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        bodyGeo.setLocalTranslation(0, hitBoxSize, 0);
        playerNode.attachChild(bodyGeo);
        playerNode.setLocalTranslation(0, 0, 0);
        physicsAdapter.registerCharacter(player, playerNode);
        this.playerSpatial = playerNode;
        root.attachChild(playerNode);
        this.player = player;
    }

    public Spatial getPlayerSpatial() {
        return playerSpatial;
    }

    public Node getPlayerNode() {
        return playerNode;
    }

    public Player getPlayer() {
        return player;
    }
}
