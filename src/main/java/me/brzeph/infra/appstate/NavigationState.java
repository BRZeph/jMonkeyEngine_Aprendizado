package me.brzeph.infra.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.events.EventBus;
import me.brzeph.events.NavigateToEvent;
import me.brzeph.events.QuitToDesktopEvent;

public class NavigationState extends BaseAppState {
    private final EventBus bus;
    private BaseAppState current;

    public NavigationState(EventBus bus) { this.bus = bus; }

    @Override
    protected void initialize(Application app) {
        bus.subscribe(NavigateToEvent.class, e -> switchTo(e.stateType()));
        bus.subscribe(QuitToDesktopEvent.class, e -> app.stop());
    }

    @Override
    protected void onEnable() {
        switchTo(MainMenuState.class);
    }

    private void switchTo(Class<? extends BaseAppState> type) {
        if (current != null) getStateManager().detach(current);
        current = resolve(type);
        if (current != null) getStateManager().attach(current);
    }

    private BaseAppState resolve(Class<? extends BaseAppState> t) {
        BaseAppState st = ServiceLocator.get(t);
        if (st != null) return st;
        try { return t.getDeclaredConstructor().newInstance(); }
        catch (Exception e) { return null; }
    }

    @Override protected void cleanup(Application application) {}
    @Override protected void onDisable() {}
}
