package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import me.brzeph.infra.events.EventBus;

import java.util.function.Consumer;

/**
 * HUD mínimo: HP e objetivo atual.
 *
 * Eventos esperados (crie estes records se ainda não existirem):
 *   public record DamageEvent(long targetId, int delta, int newHp) {}
 *   public record QuestUpdatedEvent(String text) {}
 */
public class HudState extends BaseAppState {

    private final EventBus bus;

    private SimpleApplication sapp;
    private Node hudRoot;
    private BitmapText hpText;
    private BitmapText questText;
    private BitmapText hintText;

    // Handlers para desinscrever no onDisable
    private Consumer<Object> damageHandler;
    private Consumer<Object> questHandler;

    private int currentHp = 100;

    public HudState(EventBus bus) {
        this.bus = bus;
    }

    @Override
    protected void initialize(Application app) {
        this.sapp = (SimpleApplication) app;
        this.hudRoot = new Node("HudRoot");

        BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        hpText = new BitmapText(font);
        hpText.setColor(ColorRGBA.Red);
        hpText.setLocalTranslation(20, 20 + font.getCharSet().getRenderedSize(), 0);
        hpText.setText("HP: " + currentHp);

        questText = new BitmapText(font);
        questText.setColor(ColorRGBA.White);
        questText.setLocalTranslation(20, sapp.getCamera().getHeight() - 20, 0);
        questText.setText("Objetivo: —");

        hintText = new BitmapText(font);
        hintText.setColor(ColorRGBA.Gray);
        hintText.setLocalTranslation(20, 40 + font.getCharSet().getRenderedSize(), 0);
        hintText.setText("[ESPACO] Atacar  |  [WASD] Mover");

        hudRoot.attachChild(hpText);
        hudRoot.attachChild(hintText);
        hudRoot.attachChild(questText);
    }

    @Override
    protected void onEnable() {
        sapp.getGuiNode().attachChild(hudRoot);

        // Subscrições
        damageHandler = evt -> {
            if (evt.getClass().getSimpleName().equals("DamageEvent")) {
                try {
                    int newHp = (int) evt.getClass().getMethod("newHp").invoke(evt);
                    currentHp = newHp;
                    hpText.setText("HP: " + currentHp);
                } catch (Exception ignore) {}
            }
        };
        questHandler = evt -> {
            if (evt.getClass().getSimpleName().equals("QuestUpdatedEvent")) {
                try {
                    String text = (String) evt.getClass().getMethod("text").invoke(evt);
                    questText.setText("Objetivo: " + text);
                } catch (Exception ignore) {}
            }
        };

        bus.subscribe(Object.class, o -> {
            // Routing simples (evita dependência direta das classes de evento)
            damageHandler.accept(o);
            questHandler.accept(o);
        });
    }

    @Override
    protected void onDisable() {
        hudRoot.removeFromParent();
        // EventBus: se usar um EventBus mais sofisticado, mantenha as referências dos
        // subscriptions para poder removê-las aqui. No EventBus simples que passamos,
        // não há "unsubscribe". Em produção, considere adicionar isso.
    }

    @Override protected void cleanup(Application app) {}
}
