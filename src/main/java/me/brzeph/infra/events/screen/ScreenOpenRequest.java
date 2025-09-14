package me.brzeph.infra.events.screen;

import me.brzeph.core.domain.gui.core.screens.ScreenParams;

public record ScreenOpenRequest(String key, ScreenParams params, boolean bringToFront, boolean allowMultiple) {
}
