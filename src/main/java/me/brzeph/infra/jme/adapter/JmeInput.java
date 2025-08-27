package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.JumpRequestedEvent;
import me.brzeph.infra.events.InputActionEvent;
import me.brzeph.infra.jme.adapter.utils.InputAction;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static me.brzeph.infra.jme.adapter.utils.InputAction.*;

public class JmeInput implements ActionListener {

    private final Application app;
    private final EventBus bus;
    private final Player player;

    private static final Map<String, InputAction> actionMap =
            Arrays.stream(InputAction.values())
                    .collect(Collectors.toMap(InputAction::getName, a -> a));

    public JmeInput(Player player, Application app, EventBus bus) {
        this.app = app;
        this.bus = bus;
        this.player = player;
    }

    public void bindGameplayMappings() {
        InputManager inputManager = app.getInputManager();

        // Ações de movimentação;
        inputManager.addMapping(JUMP.getName(),   new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(MOVE_FORWARD.getName() , new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_BACKWARD.getName(), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVE_LEFT.getName()    , new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT.getName()   , new KeyTrigger(KeyInput.KEY_D));

        inputManager.addListener(this,
                JUMP.getName(),
                MOVE_FORWARD.getName(), MOVE_BACKWARD.getName(),
                MOVE_LEFT.getName(), MOVE_RIGHT.getName());
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        InputAction action = actionMap.get(name);
        if (action == null) return;

        if (action == InputAction.JUMP && isPressed) {
            bus.post(new JumpRequestedEvent(1L));
        } else {
            bus.post(new InputActionEvent(player.getId(), action, isPressed));
        }
    }
}