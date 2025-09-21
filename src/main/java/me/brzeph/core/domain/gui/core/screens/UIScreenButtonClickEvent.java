package me.brzeph.core.domain.gui.core.screens;

public record UIScreenButtonClickEvent(String screenId, String widgetId, float mouseX, float mouseY, int button, String buttonId) {
}
