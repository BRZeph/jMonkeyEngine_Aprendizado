package me.brzeph.app.systems.impl.animationSystem.combo;

import me.brzeph.app.systems.SystemAbs;

import java.util.List;
import java.util.function.Predicate;

public record ComboStep(Predicate<SystemAbs> conditionToEnterThis, List<ComboStep> nextSteps, ComboStepStats stats, int priority) {
}
