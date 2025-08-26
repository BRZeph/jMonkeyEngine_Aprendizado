package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.NavigateToEvent;
import me.brzeph.infra.events.QuitToDesktopEvent;

import java.util.Objects;

public class MainMenuState extends BaseAppState {
    private EventBus bus;
    private SimpleApplication app;
    private Node uiRoot;

    private Picture bg;        // fundo
    private Picture vignette;  // vinheta sutil
    private BitmapText title, titleShadow;
    private BitmapText[] options;
    private int selected = 0;

    // animação
    private float time = 0f;

    // sons
    private AudioNode sfxMove, sfxOk;

    public MainMenuState(EventBus bus) {
        this.bus = Objects.requireNonNull(bus, "EventBus não pode ser nulo");
    }

    @Override
    protected void initialize(Application application) {
        this.app = (SimpleApplication) application;

        uiRoot = new Node("MainMenuUI");
        app.getGuiNode().attachChild(uiRoot);

        var assets = app.getAssetManager();
        var camW = app.getCamera().getWidth();
        var camH = app.getCamera().getHeight();

        // ---- Fundo ----
        bg = new Picture("bg");
        bg.setImage(assets, "Interface/bg_menu.png", true);
        bg.setWidth(camW);
        bg.setHeight(camH);
        bg.setPosition(0, 0);
        uiRoot.attachChild(bg);

//        // Vinheta leve por cima (PNG com alpha)
//        vignette = new Picture("vignette");
//        vignette.setImage(assets, "Interface/vignette.png", true);
//        vignette.setWidth(camW);
//        vignette.setHeight(camH);
//        vignette.setPosition(0, 0);
//        uiRoot.attachChild(vignette);

        // ---- Fonte ----
        BitmapFont font = assets.loadFont("Interface/Fonts/Default.fnt");

        // ---- Título com sombra (duplicar texto com offset e cor escura) ----
        titleShadow = new BitmapText(font);
        titleShadow.setText("MY RPG");
        titleShadow.setSize(font.getCharSet().getRenderedSize() * 2.2f);
        titleShadow.setColor(new ColorRGBA(0, 0, 0, 0.6f));
        centerX(titleShadow);
        titleShadow.setLocalTranslation(titleShadow.getLocalTranslation().x + 3,
                camH - 97, 0);
        uiRoot.attachChild(titleShadow);

        title = new BitmapText(font);
        title.setText("MY RPG");
        title.setSize(font.getCharSet().getRenderedSize() * 2.2f);
        title.setColor(ColorRGBA.White);
        centerX(title);
        title.setLocalTranslation(title.getLocalTranslation().x, camH - 100, 0);
        uiRoot.attachChild(title);

        // ---- Opções ----
        String[] labels = {"Start Game", "Settings", "Quit"};
        options = new BitmapText[labels.length];

        int baseY = (int)(camH * 0.60f);
        for (int i = 0; i < labels.length; i++) {
            BitmapText opt = new BitmapText(font);
            opt.setText(labels[i]); // deixamos sem prefixo, adicionamos no highlight()
            opt.setSize(font.getCharSet().getRenderedSize() * 1.5f);
            opt.setColor(new ColorRGBA(1f, 1f, 1f, 0.85f));
            centerX(opt);
            opt.setLocalTranslation(opt.getLocalTranslation().x, baseY - (i * 42), 0);
            uiRoot.attachChild(opt);
            options[i] = opt;
        }
        highlight();

//        // ---- Sons ----
//        sfxMove = new AudioNode(assets, "Sounds/ui_move.ogg", false);
//        sfxOk   = new AudioNode(assets, "Sounds/ui_ok.ogg",   false);
//        sfxMove.setPositional(false);
//        sfxOk.setPositional(false);
//        uiRoot.attachChild(sfxMove);
//        uiRoot.attachChild(sfxOk);

        // ---- Inputs ----
        var im = app.getInputManager();
        im.addMapping("MM_UP",    new KeyTrigger(KeyInput.KEY_UP),    new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("MM_DOWN",  new KeyTrigger(KeyInput.KEY_DOWN),  new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("MM_OK",    new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("MM_BACK",  new KeyTrigger(KeyInput.KEY_ESCAPE));
        im.addListener(listener, "MM_UP", "MM_DOWN", "MM_OK", "MM_BACK");

        // ---- Fade-in ----
        uiRoot.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Gui);
        uiRoot.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
        uiRoot.setLocalScale(1,1,1); // base
        setAlpha(0f); // começa invisível; vai aparecer no update()
    }

    @Override
    public void update(float tpf) {
        time += tpf;

        // Fade-in nos primeiros ~0.6s
        float a = Math.min(1f, time / 0.6f);
        setAlpha(a);

        // Pulso sutil no título
        float pulse = 1f + 0.02f * (float)Math.sin(time * 2.5); // ±2%
        title.setLocalScale(pulse);
        centerX(title);

        // Se a janela mudar de tamanho, reencaixa (simples e seguro)
        if (bg.getWidth() != app.getCamera().getWidth() || bg.getHeight() != app.getCamera().getHeight()) {
            float w = app.getCamera().getWidth();
            float h = app.getCamera().getHeight();
            bg.setWidth(w);  bg.setHeight(h);
            vignette.setWidth(w); vignette.setHeight(h);

            centerX(titleShadow); titleShadow.setLocalTranslation(titleShadow.getLocalTranslation().x + 3, h - 97, 0);
            centerX(title);       title.setLocalTranslation(title.getLocalTranslation().x, h - 100, 0);

            int baseY = (int)(h * 0.60f);
            for (int i = 0; i < options.length; i++) {
                BitmapText opt = options[i];
                centerX(opt);
                opt.setLocalTranslation(opt.getLocalTranslation().x, baseY - (i * 42), 0);
            }
        }
    }

    private final ActionListener listener = (name, isPressed, tpf) -> {
        if (isPressed) return;
        switch (name) {
            case "MM_UP" -> {
                selected = (selected + options.length - 1) % options.length;
                highlight();
                if (sfxMove != null) sfxMove.playInstance();
            }
            case "MM_DOWN" -> {
                selected = (selected + 1) % options.length;
                highlight();
                if (sfxMove != null) sfxMove.playInstance();
            }
            case "MM_OK" -> {
                if (sfxOk != null) sfxOk.playInstance();
                confirm();
            }
            case "MM_BACK" -> bus.post(new QuitToDesktopEvent());
        }
    };

    private void centerX(BitmapText bt) {
        float x = (app.getCamera().getWidth() - bt.getLineWidth()) / 2f;
        bt.setLocalTranslation(x, bt.getLocalTranslation().y, 0);
    }

    private void setAlpha(float a) {
        // aplica alpha às coisas principais
        title.setAlpha(a);
        titleShadow.setAlpha(Math.min(1f, a)); // sombra acompanha
        for (var opt : options) opt.setAlpha(a);
        // vinheta e bg normalmente ficam opacos; se quiser, aplique também
    }

    private void confirm() {
        switch (selected) {
            case 0 -> bus.post(new NavigateToEvent(GameState.class));      // -> gameplay
//            case 1 -> bus.post(new NavigateToEvent(SettingsState.class));  // -> settings (se existir)
            case 2 -> bus.post(new QuitToDesktopEvent());                  // -> sair
        }
    }

    private void highlight() {
        for (int i = 0; i < options.length; i++) {
            boolean sel = (i == selected);
            BitmapText opt = options[i];

            // texto com/sem seta
            String base = opt.getText().replaceFirst("^>\\s*", "");
            opt.setText(sel ? "> " + base : base);

            // cor e escala
            opt.setColor(sel ? ColorRGBA.White : new ColorRGBA(0.85f, 0.85f, 0.85f, 0.85f));
            float scale = sel ? 1.05f : 1.0f;
            opt.setLocalScale(scale);

            // manter centralizado após alterar texto/escala
            centerX(opt);
        }
    }

    @Override
    protected void onEnable() {
        app.getInputManager().setCursorVisible(true);
        app.getFlyByCamera().setEnabled(false); // desativa controle de câmera estilo FPS
    }

    @Override
    protected void onDisable() {
        // nada
    }

    @Override
    protected void cleanup(Application application) {
        // remove UI
        if (uiRoot != null) {
            uiRoot.removeFromParent();
            uiRoot = null;
        }
        // remove inputs
        var im = app.getInputManager();
        if (im != null) {
            im.deleteMapping("MM_UP");
            im.deleteMapping("MM_DOWN");
            im.deleteMapping("MM_OK");
            im.deleteMapping("MM_BACK");
            im.removeListener(listener);
        }
    }
}
