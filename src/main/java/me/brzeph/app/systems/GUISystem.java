package me.brzeph.app.systems;

import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.entities.ui.InventoryToggleEvent;
import me.brzeph.infra.jme.adapter.renderer.GUIRenderAdapter;

public class GUISystem {

    private final EventBus bus;
    private final GUIRenderAdapter ui;
    private boolean inventoryOpen = false;

    public GUISystem(EventBus bus, GUIRenderAdapter adapter) {
        this.bus = bus;
        this.ui  = adapter;
        initialize();
    }

    public void initialize() {
//        bus.subscribe(InventoryOpenEvent.class, e -> openInventory());
//        bus.subscribe(InventoryCloseEvent.class, e -> closeInventory());

//        bus.subscribe(HotbarShowEvent.class, e -> ui.setHotbarVisible(true));
//        bus.subscribe(HotbarHideEvent.class, e -> ui.setHotbarVisible(false));
//        bus.subscribe(MinimapShowEvent.class, e -> ui.setMinimapVisible(true));
//        bus.subscribe(MinimapHideEvent.class, e -> ui.setMinimapVisible(false));
        bus.subscribe(InventoryToggleEvent.class, this::InventoryToggleEvent);
        ui.buildStaticUI();           // cria hotbar, minimap, inventory (invisível)
        ui.setInventoryVisible(false);
        ui.setHotbarVisible(true);
        ui.setMinimapVisible(true);
    }

    public void update(float tpf) {
        ui.updateIfViewportChanged();
    }

    private void InventoryToggleEvent(InventoryToggleEvent inventoryToggleEvent) {
        inventoryOpen = inventoryToggleEvent.pressState() != 0;
        if (inventoryOpen) closeInventory(); else openInventory();
    }

    private void openInventory() {
        inventoryOpen = true;
        ui.setInventoryVisible(true);
        ui.setHotbarVisible(false);
        ui.setMinimapVisible(false);
    }

    private void closeInventory() {
        inventoryOpen = false;
        ui.setInventoryVisible(false);
        ui.setHotbarVisible(true);
        ui.setMinimapVisible(true);
    }

    public GUIRenderAdapter getUi() {
        return ui;
    }
}
