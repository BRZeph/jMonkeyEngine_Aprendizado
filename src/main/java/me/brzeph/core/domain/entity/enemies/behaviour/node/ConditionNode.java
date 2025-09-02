package me.brzeph.core.domain.entity.enemies.behaviour.node;

import me.brzeph.core.domain.entity.enemies.Monster;

import java.util.function.Function;

public class ConditionNode extends Node {
    private final Function<Monster, Boolean> condition;

    public ConditionNode(Function<Monster, Boolean> condition) {
        this.condition = condition;
    }

    @Override
    public Status tick(Monster monster) {
        return condition.apply(monster) ? Status.SUCCESS : Status.FAILURE;
    }
}


