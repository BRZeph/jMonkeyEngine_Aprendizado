package me.brzeph.bootstrap;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import me.brzeph.infra.jme.appstate.LoadingState;

public class MyGame extends SimpleApplication {

    public static void main(String[] args) {
        AppSettings cfg = new AppSettings(true);
        cfg.setTitle("My RPG");
        cfg.setVSync(true);
        cfg.setFrameRate(60);
        cfg.setResolution(1600, 900);
        cfg.setSamples(4); // MSAA
        cfg.setGammaCorrection(true);

        MyGame app = new MyGame();
        app.setSettings(cfg);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        GameModule.wire(this);
        stateManager.attach(ServiceLocator.get(LoadingState.class));
    }
}