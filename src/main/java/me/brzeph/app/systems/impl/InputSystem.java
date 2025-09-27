package me.brzeph.app.systems.impl;

import com.jme3.input.controls.ActionListener;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.domain.entity.player.Player;
import me.brzeph.events.entities.player.PlayerJumpEvent;
import me.brzeph.events.entities.player.PlayerRunEvent;
import me.brzeph.events.entities.player.PlayerWalkEvent;
import me.brzeph.events.screen.ScreenToggleRequest;

import java.util.ArrayList;
import java.util.List;

import static me.brzeph.app.service.InputService.*;
import static me.brzeph.app.service.InputService.InputAction.*;
import static me.brzeph.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY;

public class InputSystem extends SystemAbs implements ActionListener {

    private PlayerSystem playerSystem;
    private GUISystem guiSystem;
    private Player player;
    private final ArrayList<InputAction> beingHeldDown = new ArrayList<>();

    public InputSystem() {
        bindKeys(this, getApp().getInputManager());
    }

    public void initialize(){
        playerSystem = getSystem(PlayerSystem.class);
        guiSystem = getSystem(GUISystem.class);
        player = playerSystem.getPlayer();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        InputAction action = InputAction.findActionByName(name);
        if(isPressed && action != null) {
            beingHeldDown.add(action);
        } else if(!isPressed && action != null) {
            beingHeldDown.remove(action);
        }

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
//            getBus().post(new ChatToggle(player.getId(), isPressed));
        }
    }

    public boolean beingHeldDown(InputAction action){
        return beingHeldDown.contains(action);
    }

    public boolean beingHeldDown(List<InputAction> action){
        for (InputAction a : action){
            if (beingHeldDown.contains(a)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(float tpf) {
    }
}
