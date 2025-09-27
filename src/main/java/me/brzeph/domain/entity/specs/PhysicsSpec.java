package me.brzeph.domain.entity.specs;

import com.jme3.math.Vector3f;

public record PhysicsSpec(
        BodyKind   body,
        ShapeType  shape,
        Vector3f   halfExtents,      // BOX
        float      radius,           // SPHERE/CAPSULE
        float      height,           // CAPSULE altura total
        float      mass,             // 0 => STATIC
        boolean    kinematic,
        boolean    useCCD,
        float      friction,
        float      restitution,
        float      stepHeight,       // CHARACTER
        float      slopeLimitDeg     // CHARACTER
) {}
