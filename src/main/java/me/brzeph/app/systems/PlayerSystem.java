package me.brzeph.app.systems;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.service.PlayerService;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.InputActionEvent;
import me.brzeph.infra.jme.adapter.audio.JmePlayerAudio;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.jme.adapter.renderer.JmePlayerRenderer;
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

    // Estado atual de input para cada jogador
    private static class PlayerInputState {
        boolean forward, backward, left, right;
    }
    private final Map<String, PlayerInputState> inputStates = new HashMap<>();

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
    }

    private void initialize(Application app) {
        bus.subscribe(InputActionEvent.class, this::onInputAction);
    }

    // Evento genérico de input
    public void onInputAction(InputActionEvent event) {
        PlayerInputState state = inputStates.computeIfAbsent(event.playerId(), k -> new PlayerInputState());

        switch (event.action()) {
            case MOVE_FORWARD  -> state.forward  = event.pressed();
            case MOVE_BACKWARD -> state.backward = event.pressed();
            case MOVE_LEFT     -> state.left     = event.pressed();
            case MOVE_RIGHT    -> state.right    = event.pressed();

            case JUMP -> {
                if (event.pressed()) {
                    Player player = (Player) GameEntityRepository.findById(event.playerId());
                    if (player != null && playerService.canJump(player)) {
                        physicsAdapter.jumpCharacter(player);
                        playerAudio.playSound("jump.wav");
                    }
                }
            }

            default -> {
                // outras ações (dash, ataque, etc.)
            }
        }
    }

    public void update(float tpf, Camera cam) {
        for (var entry : inputStates.entrySet()) {
            Player player = (Player) GameEntityRepository.findById(entry.getKey());
            if (player == null) continue;

            PlayerInputState state = entry.getValue();

            // Obtendo a direção da câmera
            Vector3f camDir = cam.getDirection().clone().normalize();
            Vector3f camLeft = cam.getLeft().clone().normalize();

            // Calculando o vetor de movimento relativo à câmera
            Vector3f walkDir = new Vector3f();
            if (state.forward)  walkDir.addLocal(camDir);
            if (state.backward) walkDir.addLocal(camDir.negate());
            if (state.left)     walkDir.addLocal(camLeft);
            if (state.right)    walkDir.addLocal(camLeft.negate());

            walkDir.normalizeLocal();  // Normaliza a direção para manter a mesma velocidade de movimento

            // Aplicando o movimento ao personagem
            physicsAdapter.moveCharacter(player, walkDir);
            playerRenderer.updatePosition(player);  // Atualiza o visual do player
        }
    }


    public void spawnPlayer(Player player, AssetManager assetManager, Node root) {
        playerNode = new Node(player.getId());
        Box bodyBox = new Box(0.3f, 0.9f, 0.3f);
        Geometry bodyGeo = new Geometry("Body", bodyBox);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", ColorRGBA.Orange);
        bodyGeo.setMaterial(m);
        bodyGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        playerNode.attachChild(bodyGeo);
        playerNode.setLocalTranslation(0, 1, 0);
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
