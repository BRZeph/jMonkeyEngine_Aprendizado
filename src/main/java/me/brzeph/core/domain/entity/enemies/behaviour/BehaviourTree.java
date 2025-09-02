package me.brzeph.core.domain.entity.enemies.behaviour;

import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.behaviour.node.Node;

public class BehaviourTree {
    private Node root;

    public BehaviourTree(Node root) {
        this.root = root;
    }

    public void tick(Monster monster) {
        root.tick(monster);
    }
}

