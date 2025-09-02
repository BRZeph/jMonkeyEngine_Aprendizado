package me.brzeph.core.domain.entity.enemies.behaviour.node;

import me.brzeph.core.domain.entity.enemies.Monster;

import java.util.List;

public class SelectorNode extends CompositeNode {
    public SelectorNode(List<Node> children) {
        super(children);
    }

    @Override
    public Status tick(Monster monster) {
        for (Node child : children) {
            Status status = child.tick(monster);
            if (status == Status.SUCCESS) {
                return Status.SUCCESS;
            }
        }
        return Status.FAILURE;
    }
}
