package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import me.brzeph.app.ports.AssetsPort;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.NavigateToEvent;

public class LoadingState extends BaseAppState {

    private final AssetsPort assets;
    private final EventBus bus;

    public LoadingState(AssetsPort assets, EventBus bus) {
        this.assets = assets;
        this.bus = bus;
    }

    @Override
    protected void initialize(Application app) {
        assets.preload();
    }

    @Override
    protected void onEnable() {
        getStateManager().attach(ServiceLocator.get(NavigationState.class));
        bus.post(new NavigateToEvent(MainMenuState.class));
        getStateManager().detach(this);
    }

    @Override protected void onDisable() {}
    @Override protected void cleanup(Application app) {}
}
