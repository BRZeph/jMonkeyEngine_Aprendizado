package me.brzeph.core.domain.entity.enemies.behaviour.node;

import me.brzeph.core.domain.entity.enemies.Monster;

import java.util.function.Consumer;

public class ActionNode extends Node {
    private final Consumer<Monster> action;

    public ActionNode(Consumer<Monster> action) {
        this.action = action;
    }

    @Override
    public Status tick(Monster monster) {
        action.accept(monster);
        return Status.SUCCESS;
    }
}


