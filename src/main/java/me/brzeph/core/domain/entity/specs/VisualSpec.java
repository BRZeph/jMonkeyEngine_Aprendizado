package me.brzeph.core.domain.entity.specs;

import com.jme3.math.Vector3f;

public record VisualSpec(
        VisualKind kind,
        ShapeType  shape,            // PRIMITIVE: qual forma
        Vector3f   halfExtents,      // BOX (meias-dimensões)
        float      radius,           // SPHERE/CAPSULE
        float      height,           // CAPSULE/BOX visual
        String     modelKey,         // MODEL: caminho do .j3o/.glb
        String     materialKey,      // opcional: .j3m
        float      scale
) {}
