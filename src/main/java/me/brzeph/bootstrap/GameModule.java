package me.brzeph.bootstrap;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.Camera;
import me.brzeph.app.ports.*;
import me.brzeph.app.systems.CombatSystem;
import me.brzeph.app.systems.QuestSystem;
import me.brzeph.app.systems.SaveSystem;
import me.brzeph.app.systems.TimeSystem;
import me.brzeph.core.service.CombatService;
import me.brzeph.core.service.LootService;
import me.brzeph.core.service.PathService;
import me.brzeph.core.service.QuestService;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmeAudio;
import me.brzeph.infra.jme.adapter.JmeInput;
import me.brzeph.infra.jme.adapter.JmePhysics;
import me.brzeph.infra.jme.adapter.JmeRenderer;
import me.brzeph.infra.jme.appstate.*;
import me.brzeph.infra.persistence.AssetRepositoryImpl;
import me.brzeph.infra.persistence.SaveGameRepositoryJson;

public final class GameModule {

    private GameModule() {}

    public static void wire(SimpleApplication app) {
        Camera cam = app.getCamera();
        ServiceLocator.put(Camera.class, cam);

        // Config inicial da câmera (FOV, near/far, aspect ratio, etc.)
        cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);

        // ---- Infra “cross” ----
        EventBus eventBus = new EventBus();

        // ---- Ports → Adapters JME ----
        Renderer renderer = new JmeRenderer(app);
        Audio audio    = new JmeAudio(app);
        Input input    = new JmeInput(app, eventBus);
        Physics physics  = new JmePhysics(app);

        SaveGamePort savePort = new SaveGameRepositoryJson();
        AssetsPort assets   = new AssetRepositoryImpl(app.getAssetManager());

        // ---- Core services (puro Java) ----
        CombatService combatService = new CombatService();
        QuestService questService  = new QuestService();
        LootService   lootService   = new LootService();
        PathService pathService   = new PathService();

        // ---- Systems (app layer) ----
//        CombatSystem combatSystem = new CombatSystem(combatService, renderer, audio, eventBus);
//        QuestSystem  questSystem  = new QuestSystem(questService, eventBus);
//        TimeSystem   timeSystem   = new TimeSystem(eventBus);
//        SaveSystem   saveSystem   = new SaveSystem(savePort, assets, eventBus);
        CombatSystem combatSystem = new CombatSystem();
        QuestSystem  questSystem  = new QuestSystem();
        TimeSystem   timeSystem   = new TimeSystem();
        SaveSystem   saveSystem   = new SaveSystem();

        // ---- AppStates (JME) ----
        LoadingState loading = new LoadingState(assets, eventBus);
        MainMenuState menu = new MainMenuState(eventBus);
        HudState hud      = new HudState(eventBus);
        DialogueState dialogue = new DialogueState(eventBus);
        NavigationState nav = new NavigationState(eventBus);
        GameState gameState = new GameState(
                renderer, audio, input, physics, eventBus, combatSystem, questSystem, timeSystem, saveSystem, cam
        );

        // ---- Registro no ServiceLocator ----
        ServiceLocator.put(EventBus.class, eventBus);
        ServiceLocator.put(Renderer.class, renderer);
        ServiceLocator.put(Audio.class, audio);
        ServiceLocator.put(Input.class, input);
        ServiceLocator.put(Physics.class, physics);
        ServiceLocator.put(SaveGamePort.class, savePort);
        ServiceLocator.put(AssetsPort.class, assets);

        ServiceLocator.put(CombatSystem.class, combatSystem);
        ServiceLocator.put(QuestSystem.class, questSystem);
        ServiceLocator.put(TimeSystem.class, timeSystem);
        ServiceLocator.put(SaveSystem.class, saveSystem);

        ServiceLocator.put(LoadingState.class, loading);
        ServiceLocator.put(MainMenuState.class, menu);
        ServiceLocator.put(HudState.class, hud);
        ServiceLocator.put(DialogueState.class, dialogue);
        ServiceLocator.put(NavigationState.class, nav);
        ServiceLocator.put(GameState.class, gameState);
    }
}