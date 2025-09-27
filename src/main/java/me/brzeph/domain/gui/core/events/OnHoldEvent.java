package me.brzeph.domain.gui.core.events;

public record OnHoldEvent(String widgetId, float x, float y, float durationSec) {}
