package me.brzeph.core.domain.gui.core.events;

public record UIDragEndEvent  (String screenId, String widgetId, float endX, float endY, int button) {}
