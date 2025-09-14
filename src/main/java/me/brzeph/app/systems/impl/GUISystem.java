package me.brzeph.app.systems.impl;

import com.jme3.input.RawInputListener;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.core.domain.gui.core.events.UIDragEndEvent;
import me.brzeph.core.domain.gui.core.events.UIDragMoveEvent;
import me.brzeph.core.domain.gui.core.events.UIDragStartEvent;
import me.brzeph.core.domain.gui.core.others.*;
import me.brzeph.core.domain.gui.core.screens.*;

import com.jme3.scene.Node;
import me.brzeph.core.domain.gui.impl.adapters_jme.JmeFlyCamBridge;
import me.brzeph.core.domain.gui.impl.adapters_jme.UIAssetsJme;
import me.brzeph.core.domain.gui.impl.adapters_jme.UIBackendJme;
import me.brzeph.core.domain.gui.impl.inventory.InventoryServiceImpl;
import me.brzeph.core.domain.gui.impl.screens.PlayerInventoryPlugin;
import me.brzeph.infra.events.screen.*;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.*;

import static me.brzeph.core.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY;
import static me.brzeph.core.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY_CLOSE_BTN_NAME;


/**
 * Contrato e infraestrutura do sistema de UI.
 * - Estende SystemAbs (usa ServiceLocator para pegar App, AssetManager, EventBus etc.)
 * - Faz boot do mini-framework (FontManager, ScreenManager, ScreenManagerInput)
 * - Expõe API para abrir/fechar telas e registrar handlers por widgetId
 * - Assina eventos no seu EventBus tipado (subscribe)
 * - Delega à subclasse: instalação de input, before/after draw e criação de bridges/adapters
 */

public class GUISystem extends SystemAbs {

    // ---- Config de fontes ----
    public static final class FontSpec {

        public final String key, path;
        public final float size;

        public FontSpec(String key, String path, float size){
            this.key=key;
            this.path=path;
            this.size=size;
        }
    }

    // ---- Estado do sistema ----
    protected Node uiNode;
    protected FontManager fonts;
    protected ScreenManager screens;
    protected ScreenManagerInput screenInput;

    protected float mx, my;
    protected boolean mouseDown;
    protected int lastButton;
    protected boolean immediateDrawing = true;

    // registro e instâncias
    private final Map<String, Screen> instances = new HashMap<>(); // key -> única instância (singleton)
    private final Map<String, ScreenPlugin> plugins = new HashMap<>();
    private UIBackend uiBackend;
    private UIAssets uiAssets;

    // handlers de click diretos por widget
    private final Map<String, Runnable> onClickByWidget = new HashMap<>();

    private final Map<String, ScreenParams> stickyParams = new HashMap<>();
    private RawInputListener raw;

    private InventoryItem holdingItem = null;
    private float holdingItemXPos = 0f;
    private float holdingItemYPos = 0f;

    // ---------------- ciclo de vida ----------------

    public final void initialize(FontSpec... fontSpecs){
        uiNode = provideUiNode();
        uiBackend = createBackend();
        uiAssets = createAssets();
        FlyCamBridge fly  = createFlyCamBridge();

        fonts = new FontManager(uiBackend, uiAssets);
        if (fontSpecs == null || fontSpecs.length == 0){
            fonts.register("default","Interface/Fonts/Default.fnt",16f);
            fonts.register("title",  "Interface/Fonts/Default.fnt",24f);
        } else {
            for (var fs: fontSpecs) fonts.register(fs.key, fs.path, fs.size);
        }
        UICoreServiceLocator.bind(FontManager.class, fonts);

        screens = new ScreenManager(fly);
        screenInput = new ScreenManagerInput(screens, getBus());

        subscribeCoreEvents();
        installInputHooks();
        onAfterInit();

        registerDefaults();
    }

    public final void update(float tpf){
        screenInput.update(tpf, mx, my, mouseDown, lastButton);
        beforeDraw();
        screens.drawAll();
        if (holdingItem != null){
//            holdingItem.definition().getIconPath();
            uiBackend.drawRect(new Rect(holdingItemXPos - 32, holdingItemYPos - 32, 64f,64f),
                    new Color(100,100,100,1), 3f);
        }
    }

    public final void dispose(){
        removeInputHooks();
        onBeforeDispose();
    }

    // ---------------- registro de telas ----------------

    protected void registerDefaults(){
        initPlayerInventory();
    }

    private void initPlayerInventory() {
        registerPlugin(PLAYER_INVENTORY, new PlayerInventoryPlugin());
        updateScreenParams(PLAYER_INVENTORY, new ScreenParams()
                .put("gold", 512)
                .put("weight", 77.4954f)
                .put("maxWeight", 150f)
        );
        updateScreenParams(PLAYER_INVENTORY, mergeDefaultsWith());
        onClick(PLAYER_INVENTORY_CLOSE_BTN_NAME, () -> close(PLAYER_INVENTORY));
    }

    protected ScreenPlugin resolvePlugin(String key){
        return plugins.get(key);
    }

    protected final void registerPlugin(String key, ScreenPlugin plugin){
        plugins.put(key, plugin);
    }
    // ---------------- API pública simples ----------------

    public final Screen open(String key){
        return open(key, null, true, false);
    }

    public Screen open(String key, ScreenParams override, boolean bringToFront, boolean allowMultiple){
        var plugin = resolvePlugin(key);
        if (plugin == null){
            System.err.println("[GUI] Nenhum plugin registrado para key=" + key);
            return null;
        }

        if (!allowMultiple){
            Screen existing = instances.get(key);
            if (existing != null){
                if (bringToFront) screens.bringToFront(existing.id());
                return existing;
            }
        }

        ScreenParams params = finalParamsFor(key, override);

        Screen screen = plugin.build(new ScreenContext(), params);
        if (screen == null){
            System.err.println("[GUI] Plugin " + key + " retornou null");
            return null;
        }

        screen.setActive(true).setVisible(true);
        screens.register(screen);

        if (!allowMultiple) instances.put(key, screen);
        if (bringToFront) screens.bringToFront(screen.id());

        getBus().post(new ScreenOpened(key, screen.id()));
        return screen;
    }

    public void updateScreenParams(String key, ScreenParams patch){
        stickyParams.put(key, stickyParams.getOrDefault(key, new ScreenParams()).copy().putAll(patch));
        if (isOpen(key)){
            close(key);
            open(key, stickyParams.get(key), /*bring*/true, /*allowMultiple*/false);
        }
    }

    private ScreenParams finalParamsFor(String key, ScreenParams override){
        var cam = getApp().getCamera();
        return new ScreenParams()
                .put("screenW", (float) cam.getWidth())
                .put("screenH", (float) cam.getHeight())
                .putAll(stickyParams.get(key))
                .putAll(override);
    }

    private ScreenParams mergeDefaultsWith(){
        var cam = getApp().getCamera();
        return new ScreenParams()
                .put("screenW", (float) cam.getWidth())
                .put("screenH", (float) cam.getHeight());
    }

    public final void close(String key){
        Screen s = instances.get(key);
        if (s != null){
            screens.close(s.id());
            instances.remove(key);
            getBus().post(new ScreenClosed(key, s.id()));
        }
    }

    public final void toggle(String key){
        if (isOpen(key)) close(key); else open(key);
    }

    public final void bringToFront(String key){
        Screen s = instances.get(key);
        if (s != null) screens.bringToFront(s.id());
    }

    public final boolean isOpen(String key){ return instances.containsKey(key); }

    public final void onClick(String widgetId, Runnable action){
        onClickByWidget.put(widgetId, action);
    }

    // ---------------- eventos padrão ----------------

    // GUISystemAbs
    protected void subscribeCoreEvents(){
        getBus().subscribe(UIDragStartEvent.class, e -> {
            this.holdingItem = e.item();
        });

        getBus().subscribe(UIDragMoveEvent.class, e -> {
            holdingItemXPos = e.x();
            holdingItemYPos = e.y();
        });

        getBus().subscribe(UIDragEndEvent.class, e -> {
            /*
            Checar de dropar item aqui.
             */
            holdingItem = null;
            holdingItemYPos = 0;
            holdingItemXPos = 0;
        });

        getBus().subscribe(UIScreenClickEvent.class, e -> {
            Runnable r = onClickByWidget.get(e.widgetId());
            if (r != null) r.run();
        });

        getBus().subscribe(ScreenOpenRequest.class, r -> {
            open(r.key(), r.params(), r.bringToFront(), r.allowMultiple());
        });

        getBus().subscribe(ScreenCloseRequest.class, r -> {
            close(r.key());
        });

        getBus().subscribe(ScreenToggleRequest.class, r -> {
            toggle(r.key());
        });

        getBus().subscribe(ScreenBringToFrontRequest.class, r -> {
            bringToFront(r.key());
        });

        getBus().subscribe(InventoryServiceImpl.InventoryChangedEvent.class, e -> {
            Player player = (Player) GameEntityRepository.findById(e.playerId());
            if (e.area() == InventoryServiceImpl.InventoryChangedEvent.Area.COMMON){
                // Recarregue somente os slots comuns visíveis:
                // ex.: para i alterados -> InventoryAdapter.refreshCommonSlot(slots[i], port, i);
                // Se não guarda a referência dos slots, pode reconstruir o grid (custo ok se raro).
            } else {
                // EQUIPMENT: atualize apenas os slots de equipamento
            }
        });

        getBus().subscribe(InventoryServiceImpl.GoldChangedEvent.class, e -> {
            System.out.println("Chamando evento: " + e);
            if (isOpen(PLAYER_INVENTORY)) {
                updateScreenParams(PLAYER_INVENTORY, new ScreenParams().put("gold", e.gold()));
            }
        });

    }

    // ---------------- hooks dependentes da engine ----------------

    protected Node provideUiNode() {
        Node n = new Node("UIRoot");
        getApp().getGuiNode().attachChild(n);
        return n;
    }
    protected UIBackend createBackend() {
        return new UIBackendJme(getAssetManager(), uiNode);
    }

    protected UIAssets createAssets() {
        return new UIAssetsJme(getAssetManager());
    }

    protected FlyCamBridge createFlyCamBridge() {
        CameraSystem camSys = (CameraSystem) SystemAbs.getSystem(CameraSystem.class);
        return new JmeFlyCamBridge(camSys);
    }

    protected void installInputHooks() {
        raw = new RawInputListener() {
            @Override public void onMouseMotionEvent(MouseMotionEvent evt){ mx=evt.getX(); my=evt.getY(); }
            @Override public void onMouseButtonEvent(MouseButtonEvent evt){
                mouseDown = evt.isPressed();
                lastButton = evt.getButtonIndex();
                mx=evt.getX(); my=evt.getY();
            }
            @Override public void beginInput() {}
            @Override public void endInput() {}
            @Override public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {}
            @Override public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {}
            @Override public void onKeyEvent(com.jme3.input.event.KeyInputEvent evt) {}
            @Override public void onTouchEvent(com.jme3.input.event.TouchEvent evt) {}
        };
        getApp().getInputManager().addRawInputListener(raw);
    }

    protected void removeInputHooks() {
        if (raw != null) {
            getApp().getInputManager().removeRawInputListener(raw);
            raw = null;
        }
    }

    protected void beforeDraw() {
        if (immediateDrawing) uiNode.detachAllChildren();
    }

    protected void onAfterInit(){}
    protected void onBeforeDispose(){}
}


