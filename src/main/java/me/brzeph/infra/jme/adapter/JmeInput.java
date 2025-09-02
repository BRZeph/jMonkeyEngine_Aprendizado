package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.chat.ChatScroll;
import me.brzeph.infra.events.chat.ChatToggle;
import me.brzeph.infra.events.entities.player.PlayerJumpEvent;
import me.brzeph.infra.events.entities.player.PlayerRunEvent;
import me.brzeph.infra.events.entities.player.PlayerWalkEvent;
import me.brzeph.infra.events.entities.ui.InventoryToggleEvent;
import me.brzeph.infra.jme.adapter.utils.InputAction;
import me.brzeph.infra.jme.adapter.utils.InputState;

import java.util.*;
import java.util.stream.Collectors;

import static me.brzeph.infra.jme.adapter.utils.InputAction.*;

public class JmeInput implements ActionListener {

    private final Application app;
    private final EventBus bus;
    private final Player player;

    private static final Map<String, InputAction> actionMap =
            Arrays.stream(InputAction.values())
                    .collect(Collectors.toMap(InputAction::getName, a -> a));

    private final Map<String, InputState> inputStates = new HashMap<>();  // Para trackear o estado da tecla

    public JmeInput(Player player, Application app, EventBus bus) {
        this.app = app;
        this.bus = bus;
        this.player = player;
    }

    public void bindGameplayMappings() {
        InputManager inputManager = app.getInputManager();

        for(InputAction ia : InputAction.values()){
            inputManager.addMapping(ia.getName(), new KeyTrigger(ia.getInput()));
        }

        inputManager.addListener(
                this,
                Arrays.stream(InputAction.values())
                        .map(InputAction::getName)
                        .toArray(String[]::new)
        );
    }


    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        InputAction action = actionMap.get(name);
        if (action == null) return;

        action.press();

        InputState inputState = getInputState(name, isPressed);

        if (action == InputAction.JUMP) {
            bus.post(new PlayerJumpEvent(player.getId(), inputState));
        } else if (action == InputAction.MOVE_FORWARD || action == InputAction.MOVE_BACKWARD ||
                action == InputAction.MOVE_LEFT || action == InputAction.MOVE_RIGHT) {
            bus.post(new PlayerWalkEvent(player.getId(), action.getDirection(), inputState));
        } else if (action == TRIGGER_RUN){
            bus.post(new PlayerRunEvent(player.getId(), inputState));
        } else if (action == TOGGLE_INVENTORY){
            bus.post(new InventoryToggleEvent(player.getId(), action.getPressedState()));
        } else if (action == TOGGLE_CHAT) {
            bus.post(new ChatToggle(action.getPressedState()));
        } else if (action == CHAT_PG_DOWN || action == CHAT_PG_UP) {
            bus.post(new ChatScroll(action.getDirection()));
        }
    }

    private InputState getInputState(String name, boolean isPressed) {
        InputState currentState = inputStates.get(name);

        if (isPressed) {
            if (currentState == InputState.RELEASED || currentState == null) {
                inputStates.put(name, InputState.PRESSED);
                return InputState.PRESSED;
            }
            inputStates.put(name, InputState.HOLDING);
            return InputState.HOLDING;
        } else {
            inputStates.put(name, InputState.RELEASED);
            return InputState.RELEASED;
        }
    }
}