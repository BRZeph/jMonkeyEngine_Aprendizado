package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import me.brzeph.app.ports.Input;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.JumpRequestedEvent;
import me.brzeph.infra.events.MoveKeyEvent;
import me.brzeph.infra.jme.adapter.utils.Direction;

import static me.brzeph.infra.constants.InputKeysConstants.*;

public class JmeInput implements Input, ActionListener {

    private final Application app;
    private final EventBus bus;

    public JmeInput(Application app, EventBus bus) {
        this.app = app; this.bus = bus;
    }

    @Override
    public void bindGameplayMappings(EventBus bus) {
        var inputManager = app.getInputManager();

        // Ações;
        inputManager.addMapping(JUMP,   new KeyTrigger(KeyInput.KEY_SPACE));

        // Movimento contínuo
        inputManager.addMapping(MOVE_FORWARD , new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVE_LEFT    , new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT   , new KeyTrigger(KeyInput.KEY_D));

        // Registrar listeners
        inputManager.addListener(this,
                JUMP,
                MOVE_FORWARD, MOVE_BACKWARD,
                MOVE_LEFT, MOVE_RIGHT);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case JUMP -> {
                if (isPressed) {
                    bus.post(new JumpRequestedEvent(1L));
                }
            }
            case MOVE_FORWARD  -> bus.post(new MoveKeyEvent(1L, Direction.FORWARD,  isPressed));
            case MOVE_BACKWARD -> bus.post(new MoveKeyEvent(1L, Direction.BACKWARD, isPressed));
            case MOVE_LEFT     -> bus.post(new MoveKeyEvent(1L, Direction.LEFT,     isPressed));
            case MOVE_RIGHT    -> bus.post(new MoveKeyEvent(1L, Direction.RIGHT,    isPressed));
        }
    }
}