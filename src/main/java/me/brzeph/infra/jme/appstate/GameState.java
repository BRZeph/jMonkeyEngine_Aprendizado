package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import me.brzeph.app.SystemsWiring;
import me.brzeph.app.ports.Audio;
import me.brzeph.app.ports.Input;
import me.brzeph.app.ports.Physics;
import me.brzeph.app.ports.Renderer;
import me.brzeph.app.systems.CombatSystem;
import me.brzeph.app.systems.QuestSystem;
import me.brzeph.app.systems.SaveSystem;
import me.brzeph.app.systems.TimeSystem;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.jme.adapter.JmePhysics;
import me.brzeph.infra.jme.factory.WorldLoader;

public class GameState extends BaseAppState {
    /*
        Analogamente falando, GameState é o mesmo que Level1Screen no proj_3.
        Telas que não envolvem diretamente gameplay (como menus) não são administradas aqui, mas sim em outros States.
        HudState é o que vai dar o overlay de dados criando barras de vida e tudo mais.
        HUD -> mostrar dados.
        GUI -> envolve interação, então existe lógica.
     */
    private final Renderer renderer;
    private final Audio audio;
    private final Input input;
    private final Physics physics;
    private final EventBus bus;

    private final CombatSystem combatSystem;
    private final QuestSystem questSystem;
    private final TimeSystem timeSystem;
    private final SaveSystem saveSystem;

    public GameState(Renderer renderer, Audio audio, Input input, Physics physics, EventBus bus,
                     CombatSystem combatSystem, QuestSystem questSystem,
                     TimeSystem timeSystem, SaveSystem saveSystem) {
        this.renderer = renderer;
        this.audio = audio;
        this.input = input;
        this.physics = physics;
        this.bus = bus;
        this.combatSystem = combatSystem;
        this.questSystem = questSystem;
        this.timeSystem = timeSystem;
        this.saveSystem = saveSystem;
    }

    public interface Factory {
        GameState create();
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication sapp = (SimpleApplication) app;

        // 1) cria e anexa o BulletAppState ao stateManager
        BulletAppState bullet = new BulletAppState();
        getStateManager().attach(bullet);

        // 2) passa a referência do bullet para o adapter de física
        ((JmePhysics) physics).setBullet(bullet);

        // 3) carrega o mundo (com terrain, luzes, etc.)
        WorldLoader.load(sapp.getAssetManager(), sapp.getRootNode(), physics);

        // 4) HUD
        getStateManager().attach(ServiceLocator.get(HudState.class));

        // 5) input mappings
        input.bindGameplayMappings(bus);

        // 6) liga os systems no EventBus
        SystemsWiring.register(bus, combatSystem, questSystem, timeSystem, saveSystem);
    }

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
    @Override protected void cleanup(Application app) {}
}