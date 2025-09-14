package me.brzeph.bootstrap;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.appstate.*;
import me.brzeph.infra.persistence.AssetRepositoryImpl;
import me.brzeph.infra.persistence.SaveGameRepositoryJson;

public final class GameModule {

    private GameModule() {}

    public static void wire(SimpleApplication app) {
        // ---- Infra “cross” ----
        EventBus eventBus = new EventBus();
        BulletAppState bullet = new BulletAppState();

        bullet.setThreadingType(BulletAppState.ThreadingType.SEQUENTIAL); // single-threaded
        app.getStateManager().attach(bullet); // Linkar bullet antes de GameState.

        SaveGameRepositoryJson savePort = new SaveGameRepositoryJson();
        AssetRepositoryImpl assets   = new AssetRepositoryImpl(app.getAssetManager());

        // ---- AppStates (JME) ----
        LoadingState loading   = new LoadingState(assets, eventBus);
        MainMenuState menu     = new MainMenuState(eventBus);
        DialogueState dialogue = new DialogueState(eventBus);
        NavigationState nav    = new NavigationState(eventBus);
        GameState gameState    = new GameState(eventBus, bullet);

        // ---- Registro no ServiceLocator ----
        ServiceLocator.put(EventBus.class, eventBus);
        ServiceLocator.put(BulletAppState.class, bullet);
        ServiceLocator.put(SaveGameRepositoryJson.class, savePort);
        ServiceLocator.put(AssetRepositoryImpl.class, assets);
        ServiceLocator.put(LoadingState.class, loading);
        ServiceLocator.put(MainMenuState.class, menu);
        ServiceLocator.put(DialogueState.class, dialogue);
        ServiceLocator.put(NavigationState.class, nav);
        ServiceLocator.put(GameState.class, gameState);
    }
}