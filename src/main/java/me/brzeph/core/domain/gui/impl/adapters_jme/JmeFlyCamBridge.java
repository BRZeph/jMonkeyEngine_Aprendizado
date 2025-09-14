package me.brzeph.core.domain.gui.impl.adapters_jme;

// Implementação jME (use no seu Main)
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import me.brzeph.app.systems.impl.CameraSystem;
import me.brzeph.core.domain.gui.core.screens.FlyCamBridge;

public final class JmeFlyCamBridge implements FlyCamBridge {
    private final SimpleApplication app;
    private final CameraSystem camSys;

    public JmeFlyCamBridge(CameraSystem camSys){
        this.camSys = camSys;
        this.app = camSys.getApp();
    }

    @Override
    public boolean isEnabled() {
        return app.getFlyByCamera().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled){
        // “câmera habilitada” == modo gameplay (UI off)
        camSys.setUiActive(!enabled);
    }

    @Override
    public boolean isCursorVisible() {
        InputManager im = app.getInputManager();
        return im != null && im.isCursorVisible();
    }

    @Override
    public void setCursorVisible(boolean visible){
        camSys.setUiActive(visible);
    }
}

