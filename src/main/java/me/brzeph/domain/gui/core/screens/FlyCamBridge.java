package me.brzeph.domain.gui.core.screens;

// Engine-agnostic
public interface FlyCamBridge {
    boolean isEnabled();
    void setEnabled(boolean enabled);
    boolean isCursorVisible();
    void setCursorVisible(boolean visible);
}

