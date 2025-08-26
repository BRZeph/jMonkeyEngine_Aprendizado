package me.brzeph.infra.jme.appstate;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import me.brzeph.infra.events.EventBus;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado de diálogo minimalista:
 * - Escuta DialogueRequestedEvent(text, choices: List<String>)
 * - Exibe texto e lista numerada de escolhas
 * - [1..9] publica DialogueChoiceSelectedEvent(index, label)
 */
public class DialogueState extends BaseAppState implements ActionListener {

    private final EventBus bus;

    private SimpleApplication sapp;
    private Node uiRoot;
    private BitmapText textArea;
    private final List<BitmapText> choiceTexts = new ArrayList<>();
    private List<String> currentChoices = List.of();

    public DialogueState(EventBus bus) {
        this.bus = bus;
    }

    @Override
    protected void initialize(Application app) {
        this.sapp = (SimpleApplication) app;
        this.uiRoot = new Node("DialogueUI");

        BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        textArea = new BitmapText(font);
        textArea.setColor(ColorRGBA.White);
        textArea.setLocalTranslation(50, sapp.getCamera().getHeight() - 150, 0);
        textArea.setText("");

        uiRoot.attachChild(textArea);
    }

    @Override
    protected void onEnable() {
        sapp.getGuiNode().attachChild(uiRoot);

        // Mapeia teclas 1..9 para escolhas
        var im = sapp.getInputManager();
        for (int i = 1; i <= 9; i++) {
            String map = "DLG_CHOICE_" + i;
            im.addMapping(map, new KeyTrigger(KeyInput.KEY_1 + (i - 1)));
            im.addListener(this, map);
        }

        // Inscreve-se a pedidos de diálogo
        bus.subscribe(Object.class, evt -> {
            if (evt.getClass().getSimpleName().equals("DialogueRequestedEvent")) {
                try {
                    String text = (String) evt.getClass().getMethod("text").invoke(evt);
                    Object arr = evt.getClass().getMethod("choices").invoke(evt);
                    List<String> choices = toStringList(arr);
                    showDialogue(text, choices);
                } catch (Exception ignore) {}
            }
        });
    }

    @Override
    protected void onDisable() {
        uiRoot.removeFromParent();
        var im = sapp.getInputManager();
        for (int i = 1; i <= 9; i++) {
            String map = "DLG_CHOICE_" + i;
            im.deleteMapping(map);
        }
        im.removeListener(this);
        clearChoices();
    }

    @Override protected void cleanup(Application app) {}

    private void showDialogue(String text, List<String> choices) {
        textArea.setText(text);
        clearChoices();

        this.currentChoices = choices != null ? choices : List.of();
        BitmapFont font = textArea.getFont();
        float baseY = textArea.getLocalTranslation().y - 40;

        for (int i = 0; i < currentChoices.size(); i++) {
            BitmapText line = new BitmapText(font);
            line.setColor(ColorRGBA.Yellow);
            line.setText((i + 1) + ") " + currentChoices.get(i));
            line.setLocalTranslation(60, baseY - i * 25, 0);
            choiceTexts.add(line);
            uiRoot.attachChild(line);
        }
    }

    private void clearChoices() {
        for (BitmapText t : choiceTexts) t.removeFromParent();
        choiceTexts.clear();
        currentChoices = List.of();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (name.startsWith("DLG_CHOICE_")) {
            int idx = Integer.parseInt(name.substring("DLG_CHOICE_".length())) - 1;
            if (idx >= 0 && idx < currentChoices.size()) {
                String label = currentChoices.get(idx);
                // Publica seleção de escolha
                // (crie record DialogueChoiceSelectedEvent(int index, String label))
                try {
                    Class<?> evt = Class.forName("me.brzeph.infra.events.DialogueChoiceSelectedEvent");
                    var ctor = evt.getDeclaredConstructor(int.class, String.class);
                    Object instance = ctor.newInstance(idx, label);
                    bus.post(instance);
                } catch (Exception ignore) {}
                // Opcional: limpar a UI depois de escolher
                // clearChoices();
            }
        }
    }

    private static List<String> toStringList(Object possiblyListOrArray) {
        if (possiblyListOrArray == null) return List.of();
        if (possiblyListOrArray instanceof List<?> l) {
            List<String> out = new ArrayList<>();
            for (Object o : l) out.add(String.valueOf(o));
            return out;
        }
        if (possiblyListOrArray.getClass().isArray()) {
            int len = Array.getLength(possiblyListOrArray);
            List<String> out = new ArrayList<>(len);
            for (int i = 0; i < len; i++) out.add(String.valueOf(Array.get(possiblyListOrArray, i)));
            return out;
        }
        return List.of(String.valueOf(possiblyListOrArray));
    }
}
