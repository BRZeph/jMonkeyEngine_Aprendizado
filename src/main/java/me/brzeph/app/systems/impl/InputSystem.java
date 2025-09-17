package me.brzeph.app.systems.impl;

import com.jme3.input.controls.ActionListener;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.infra.events.chat.ChatToggle;
import me.brzeph.infra.events.entities.player.PlayerJumpEvent;
import me.brzeph.infra.events.entities.player.PlayerRunEvent;
import me.brzeph.infra.events.entities.player.PlayerWalkEvent;
import me.brzeph.infra.events.screen.ScreenToggleRequest;

import static me.brzeph.app.service.InputService.*;
import static me.brzeph.app.service.InputService.InputAction.*;
import static me.brzeph.core.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY;

public class InputSystem extends SystemAbs implements ActionListener {

    private final PlayerSystem playerSystem;
    private final GUISystem guiSystem;
    private final Player player;

    public InputSystem() {
        bindKeys(this, getApp().getInputManager());
        playerSystem = (PlayerSystem) SystemAbs.getSystem(PlayerSystem.class);
        guiSystem = (GUISystem) SystemAbs.getSystem(GUISystem.class);
        player = playerSystem.getPlayer();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        /*
        Quando for fazer multiplayer, os eventos talvez possam ter o nome:
        [event_name]_[player_id] ou algo do tipo, desta forma posso identificar o entity aqui.
         */
        /*
            Fazer um bando de if usando os sistemas e foda-se.
            if (playerSystem.getPlayer().isInventoryOpen())
         */
        InputAction action = InputAction.findActionByName(name);
        if (action == SPACE){
            getBus().post(new PlayerJumpEvent(player.getId(), isPressed));
        }
        if(!guiSystem.isOpen(PLAYER_INVENTORY)) { // Primeiro o que excluir...
            if (action == Letter_W || action == Letter_A
                    || action == Letter_S || action == Letter_D) { // ... depois o click.
                getBus().post(new PlayerWalkEvent(player.getId(), action, isPressed));
            }
        }
        if (action == SHIFT){
            getBus().post(new PlayerRunEvent(player.getId(), isPressed));
        }
        if (action == Letter_E && !isPressed){
            getBus().post(new ScreenToggleRequest(PLAYER_INVENTORY));
        }
        if (action == ENTER){
            getBus().post(new ChatToggle(player.getId(), isPressed));
        }
    }

    @Override
    public void update(float tpf) {
    }
}
