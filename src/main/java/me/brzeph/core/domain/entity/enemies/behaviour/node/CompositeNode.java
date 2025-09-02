package me.brzeph.core.domain.entity.enemies.behaviour.node;

import java.util.List;

public abstract class CompositeNode extends Node {
    protected List<Node> children;

    public CompositeNode(List<Node> children) {
        this.children = children;
    }
}

