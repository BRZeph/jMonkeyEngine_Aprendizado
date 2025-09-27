package me.brzeph.events.screen;

import me.brzeph.domain.gui.core.screens.ScreenParams;

public record ScreenOpenRequest(String key, ScreenParams params, boolean bringToFront, boolean allowMultiple) {
}
