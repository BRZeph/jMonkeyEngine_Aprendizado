package me.brzeph.app.systems;

import com.jme3.app.Application;
import com.jme3.math.Vector3f;
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

    public void update(float tpf) {
        for (var entry : inputStates.entrySet()) {
            String playerId = entry.getKey();
            Player player = (Player) GameEntityRepository.findById(playerId);
            if (player == null) continue;

            PlayerInputState state = entry.getValue();

            Vector3f walkDir = playerService.calculateWalkDirection(
                    player,
                    state.forward, state.backward, state.left, state.right
            );

            physicsAdapter.moveCharacter(player, walkDir);
            playerRenderer.updatePosition(player);
        }
    }
}
