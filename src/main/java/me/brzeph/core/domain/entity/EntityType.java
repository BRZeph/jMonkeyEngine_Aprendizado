package me.brzeph.core.domain.entity;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import me.brzeph.core.domain.entity.specs.*;

public enum EntityType {
    PLAYER_DEFAULT(new EntityBlueprint(
            new VisualSpec(
                    VisualKind.PRIMITIVE,
                    ShapeType.BOX,
                    new Vector3f(0.60f, 0.9f, 0.60f), // Isso cria visual com tamanho 1.2f, 1.8f, 1.2f
                    // halfExtents: BOX começa no "meio" então o tamanho passado é metade.
                    0f,                                 // radius (não usado em BOX)
                    0f,                                 // height visual (não usado em BOX)
                    null,                               // modelKey (PRIMITIVE não usa)
                    null,                               // materialKey (ou "Materials/OrcBrute.j3m" se quiser)
                    1.0f                                // scale
            ),
            new PhysicsSpec(BodyKind.CHARACTER, ShapeType.CAPSULE, null,
                    0.60f, 1.80f, 80f, false, false, 0.6f, 0.05f, 0.4f, 60f)
    )),

    ORC_BRUTE(new EntityBlueprint( // VisualKind.PRIMITIVE não precisa de assets externos por definição.
            new VisualSpec(
                    VisualKind.PRIMITIVE,
                    ShapeType.BOX,
                    new Vector3f(0.60f, 1.10f, 0.60f), // halfExtents: width=1.2, height=2.2, depth=1.2
                    0f,                                 // radius (não usado em BOX)
                    0f,                                 // height visual (não usado em BOX)
                    null,                               // modelKey (PRIMITIVE não usa)
                    null,                               // materialKey (ou "Materials/OrcBrute.j3m" se quiser)
                    1.0f                                // scale
            ),
            new PhysicsSpec(
                    BodyKind.CHARACTER,
                    ShapeType.CAPSULE,
                    null,         // halfExtents (não usado em CAPSULE)
                    0.60f,        // radius da cápsula
                    2.20f,        // altura total da cápsula
                    140f,         // massa
                    false,        // kinematic
                    false,        // useCCD
                    1.0f,         // friction
                    0.0f,         // restitution
                    0.5f,         // stepHeight (se usar Character legado; para BCC pode ignorar)
                    55f           // slopeLimitDeg (idem observação acima)
            )
    )),

    DROPPED_COIN(new EntityBlueprint(
            new VisualSpec(VisualKind.PRIMITIVE, ShapeType.SPHERE, null,
                    0.15f, 0f, null, null, 1f),
            new PhysicsSpec(BodyKind.GHOST, ShapeType.SPHERE, null,
                    0.75f, 0f, 0f, true, false, 0f, 0f, 0f, 0f)
    )),
    /** fallback para qualquer item dropado sem prefab específico */
    DEFAULT_ITEM_PICKUP(
            new EntityBlueprint(
                    new VisualSpec(
                            VisualKind.MODEL,
                            ShapeType.SPHERE,
                            new Vector3f(0.25f, 0.25f, 0.25f),
                            0.25f,
                            0.0f,
                            "Models/pickups/generic_bag.j3o",
                            null,
                            1.0f
                    ),
                    new PhysicsSpec(
                            BodyKind.GHOST,
                            ShapeType.SPHERE,
                            new Vector3f(0.0f,0.0f,0.0f),
                            0.25f,
                            0.0f,
                            0.0f,
                            true,
                            false,
                            0.5f,
                            0.0f,
                            0.0f,
                            0.0f
                    )
            )
    );

    private final EntityBlueprint bp;

    EntityType(EntityBlueprint bp) {
        this.bp = bp;
    }

    public EntityBlueprint blueprint() {
        return bp;
    }
}
