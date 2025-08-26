package me.brzeph.infra.events;

import me.brzeph.infra.jme.adapter.utils.Direction;

public record MoveKeyEvent(long playerId, Direction direction, boolean pressed) { }
