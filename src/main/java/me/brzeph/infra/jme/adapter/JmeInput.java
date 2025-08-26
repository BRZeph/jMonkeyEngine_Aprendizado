package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import me.brzeph.app.ports.Input;
import me.brzeph.infra.events.EventBus;

public class JmeInput implements Input, ActionListener {

    private final Application app;
    private final EventBus bus;

    public JmeInput(Application app, EventBus bus) {
        this.app = app; this.bus = bus;
    }

    @Override public void bindGameplayMappings(EventBus bus) {
        var inputManager = app.getInputManager();
        inputManager.addMapping("ATTACK", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "ATTACK");
    }

    @Override public void onAction(String name, boolean isPressed, float tpf) {
        if ("ATTACK".equals(name) && isPressed) {
//            bus.post(new AttackRequestedEvent(/*playerId*/ 1L, /*target?*/ null));
        }
    }
}