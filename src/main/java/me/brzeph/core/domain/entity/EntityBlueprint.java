package me.brzeph.core.domain.entity;

import me.brzeph.core.domain.entity.specs.PhysicsSpec;
import me.brzeph.core.domain.entity.specs.VisualSpec;

public record EntityBlueprint(
        VisualSpec visual,
        PhysicsSpec physics
) {}
