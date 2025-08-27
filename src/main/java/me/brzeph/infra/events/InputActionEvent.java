package me.brzeph.infra.events;

import me.brzeph.infra.jme.adapter.utils.InputAction;

public record InputActionEvent(String playerId, InputAction action, boolean pressed) { }