package me.brzeph.app.service;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import me.brzeph.app.systems.impl.InputSystem;

import java.util.Arrays;

public class InputService {

    public static void bindKeys(InputSystem system, InputManager inputManager) {
        for(InputAction ia : InputAction.values()){
            inputManager.addMapping(ia.getName(), new KeyTrigger(ia.getInput()));
        }
        inputManager.addListener(
                system,
                Arrays.stream(InputAction.values())
                        .map(InputAction::getName)
                        .toArray(String[]::new)
        );
    }

    public enum InputAction {
        Letter_A("Printable_A", KeyInput.KEY_A),
        Letter_B("Printable_B", KeyInput.KEY_B),
        Letter_C("Printable_C", KeyInput.KEY_C),
        Letter_D("Printable_D", KeyInput.KEY_D),
        Letter_E("Printable_E", KeyInput.KEY_E),
        Letter_F("Printable_F", KeyInput.KEY_F),
        Letter_G("Printable_G", KeyInput.KEY_G),
        Letter_H("Printable_H", KeyInput.KEY_H),
        Letter_I("Printable_I", KeyInput.KEY_I),
        Letter_J("Printable_J", KeyInput.KEY_J),
        Letter_K("Printable_K", KeyInput.KEY_K),
        Letter_L("Printable_L", KeyInput.KEY_L),
        Letter_M("Printable_M", KeyInput.KEY_M),
        Letter_N("Printable_N", KeyInput.KEY_N),
        Letter_O("Printable_O", KeyInput.KEY_O),
        Letter_P("Printable_P", KeyInput.KEY_P),
        Letter_Q("Printable_Q", KeyInput.KEY_Q),
        Letter_R("Printable_R", KeyInput.KEY_R),
        Letter_S("Printable_S", KeyInput.KEY_S),
        Letter_T("Printable_T", KeyInput.KEY_T),
        Letter_U("Printable_U", KeyInput.KEY_U),
        Letter_V("Printable_V", KeyInput.KEY_V),
        Letter_W("Printable_W", KeyInput.KEY_W),
        Letter_X("Printable_X", KeyInput.KEY_X),
        Letter_Y("Printable_Y", KeyInput.KEY_Y),
        Letter_Z("Printable_Z", KeyInput.KEY_Z),

        // Teclas numéricas
        Numeric_0("Numeric_0", KeyInput.KEY_0),
        Numeric_1("Numeric_1", KeyInput.KEY_1),
        Numeric_2("Numeric_2", KeyInput.KEY_2),
        Numeric_3("Numeric_3", KeyInput.KEY_3),
        Numeric_4("Numeric_4", KeyInput.KEY_4),
        Numeric_5("Numeric_5", KeyInput.KEY_5),
        Numeric_6("Numeric_6", KeyInput.KEY_6),
        Numeric_7("Numeric_7", KeyInput.KEY_7),
        Numeric_8("Numeric_8", KeyInput.KEY_8),
        Numeric_9("Numeric_9", KeyInput.KEY_9),

        // Teclas de controle
        SPACE("Control_SPACE", KeyInput.KEY_SPACE),
        ENTER("Control_ENTER", KeyInput.KEY_RETURN),
        BACKSPACE("Control_BACKSPACE", KeyInput.KEY_BACK),
        TAB("Control_TAB", KeyInput.KEY_TAB),
        SHIFT("Control_SHIFT", KeyInput.KEY_LSHIFT),
        CONTROL("Control_CONTROL", KeyInput.KEY_LCONTROL),
        ALT("Control_ALT", KeyInput.KEY_LMENU),
        ESCAPE("Control_ESCAPE", KeyInput.KEY_ESCAPE),

        // Função (F1 - F12)
        F1("F1", KeyInput.KEY_F1),
        F2("F2", KeyInput.KEY_F2),
        F3("F3", KeyInput.KEY_F3),
        F4("F4", KeyInput.KEY_F4),
        F5("F5", KeyInput.KEY_F5),
        F6("F6", KeyInput.KEY_F6),
        F7("F7", KeyInput.KEY_F7),
        F8("F8", KeyInput.KEY_F8),
        F9("F9", KeyInput.KEY_F9),
        F10("F10", KeyInput.KEY_F10),
        F11("F11", KeyInput.KEY_F11),
        F12("F12", KeyInput.KEY_F12),

        // Teclas de navegação
        UP("UP", KeyInput.KEY_UP),
        DOWN("DOWN", KeyInput.KEY_DOWN),
        LEFT("LEFT", KeyInput.KEY_LEFT),
        RIGHT("RIGHT", KeyInput.KEY_RIGHT),
        HOME("HOME", KeyInput.KEY_HOME),
        END("END", KeyInput.KEY_END),
        PAGE_UP("PAGE_UP", KeyInput.KEY_PRIOR),
        PAGE_DOWN("PAGE_DOWN", KeyInput.KEY_NEXT);

        private final String name;
        private final int input;

        InputAction(String name, int input) {
            this.input = input;
            this.name = name;
        }

        public static InputAction findActionByName(String name){
            for (InputAction ia : InputAction.values()){
                if (ia.getName().equals(name)){
                    return ia;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public int getInput() {
            return input;
        }
    }
}
