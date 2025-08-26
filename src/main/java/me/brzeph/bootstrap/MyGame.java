package me.brzeph.bootstrap;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import me.brzeph.infra.jme.appstate.LoadingState;

public class MyGame extends SimpleApplication {
    /*
    começo da execução
    MyGame.main()
    MyGame.simpleInitApp()
        GameModule.wire(this); // inicializa tudo.
        stateManager.attach() // muda a tela para LoadingState.
    LoadingState.Initialize()
        LoadingState.onEnable()
        getStateManager().attach(gameFactory.create()); // Muda para a tela GameState (ela contém GameFactory).
        getStateManager().detach(this); // Sai da tela atual.
    GameState.Initialize()
        GameState.onEnable()
     */

    public static void main(String[] args) {
        AppSettings cfg = new AppSettings(true);
        cfg.setTitle("My RPG");
        cfg.setVSync(true);
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
        // Bootstrap: registra dependências e entra no primeiro estado
        GameModule.wire(this); // faz o wiring de todas as dependências no ServiceLocator.
        stateManager.attach(ServiceLocator.get(LoadingState.class));
    }
}