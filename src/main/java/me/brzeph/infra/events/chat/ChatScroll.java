package me.brzeph.infra.events.chat;

import me.brzeph.infra.jme.adapter.utils.InputAction;

public record ChatScroll(InputAction.Direction direction) {
}
