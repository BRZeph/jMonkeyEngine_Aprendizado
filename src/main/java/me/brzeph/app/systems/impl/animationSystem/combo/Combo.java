package me.brzeph.app.systems.impl.animationSystem.combo;

import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.GUISystem;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record Combo(WeaponClass clazz, List<ComboStep> steps) {
    public static Combo getCombo(WeaponClass clazz){
        switch (clazz){
            case OFF_HAND -> {
                return new Combo(WeaponClass.OFF_HAND, List.of(
                        new ComboStep(
                                systemAbs -> {
                                    if (systemAbs.getApp() != null) {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }, null, null, 0
                        ))
                );
            }
            case SWORD -> {
            }
            case LANCE -> {
            }
            case AXE -> {
            }
            case BOW -> {
            }
        }
        return null;
    }

    public void test(){
        Combo combo = getCombo(WeaponClass.OFF_HAND);

        ComboStep current = combo.steps().getFirst();
        /*
            Current será variável do controller.
         */

        ComboStep nextOverride = current.nextSteps().stream()
                .filter(comboStep -> comboStep.conditionToEnterThis().test(new GUISystem()))
                .findFirst().orElse(null);
    }
}
