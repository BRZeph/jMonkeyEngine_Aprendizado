package me.brzeph.bootstrap;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.Camera;
import me.brzeph.app.systems.*;
import me.brzeph.core.service.*;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmeAudio;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.jme.adapter.physics.JmeWorldPhysics;
import me.brzeph.infra.jme.adapter.JmeRenderer;
import me.brzeph.infra.jme.appstate.*;
import me.brzeph.infra.persistence.AssetRepositoryImpl;
import me.brzeph.infra.persistence.SaveGameRepositoryJson;

public final class GameModule {

    private GameModule() {}

    public static void wire(SimpleApplication app) {
        /*
        Observação: inicializar Systems dentro dos States.
         */

        // ---- Infra “cross” ----
        EventBus eventBus = new EventBus();

        SaveGameRepositoryJson savePort = new SaveGameRepositoryJson();
        AssetRepositoryImpl assets   = new AssetRepositoryImpl(app.getAssetManager());

        // ---- AppStates (JME) ----
        LoadingState loading = new LoadingState(assets, eventBus);
        MainMenuState menu = new MainMenuState(eventBus);
        HudState hud      = new HudState(eventBus);
        DialogueState dialogue = new DialogueState(eventBus);
        NavigationState nav = new NavigationState(eventBus);
        GameState gameState = new GameState(eventBus);

        // ---- Registro no ServiceLocator ----
        ServiceLocator.put(EventBus.class, eventBus);
        ServiceLocator.put(SaveGameRepositoryJson.class, savePort);
        ServiceLocator.put(AssetRepositoryImpl.class, assets);
        ServiceLocator.put(LoadingState.class, loading);
        ServiceLocator.put(MainMenuState.class, menu);
        ServiceLocator.put(HudState.class, hud);
        ServiceLocator.put(DialogueState.class, dialogue);
        ServiceLocator.put(NavigationState.class, nav);
        ServiceLocator.put(GameState.class, gameState);
    }
}