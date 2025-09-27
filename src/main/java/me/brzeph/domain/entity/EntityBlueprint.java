package me.brzeph.domain.entity;

import me.brzeph.app.systems.impl.animationSystem.AnimationSpec;
import me.brzeph.domain.entity.specs.PhysicsSpec;
import me.brzeph.domain.entity.specs.VisualSpec;

public record EntityBlueprint(
        VisualSpec visual,
        PhysicsSpec physics,
        AnimationSpec anim
) {}
