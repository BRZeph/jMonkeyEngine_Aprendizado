package me.brzeph.domain.entity.enemies.behaviour.node;

import me.brzeph.domain.entity.enemies.Monster;

public abstract class Node {
    public enum Status { SUCCESS, FAILURE, RUNNING }
    public abstract Status tick(Monster monster);
}


