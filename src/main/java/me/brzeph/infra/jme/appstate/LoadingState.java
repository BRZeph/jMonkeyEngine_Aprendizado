package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.NavigateToEvent;
import me.brzeph.infra.persistence.AssetRepositoryImpl;

public class LoadingState extends BaseAppState {

    private final AssetRepositoryImpl assets;
    private final EventBus bus;

    public LoadingState(AssetRepositoryImpl assets, EventBus bus) {
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
