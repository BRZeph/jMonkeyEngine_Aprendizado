package me.brzeph.domain.entity;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import me.brzeph.app.systems.impl.animationSystem.AnimationSpec;
import me.brzeph.domain.entity.specs.*;

import java.util.List;

public enum EntityType {
    PLAYER_DEFAULT(new EntityBlueprint(
            new VisualSpec(
                    VisualKind.PRIMITIVE,
                    ShapeType.BOX,
                    new Vector3f(0.60f, 0.9f, 0.60f), // Isso cria visual com tamanho 1.2f, 1.8f, 1.2f
                    0f,                                 // radius (não usado em BOX)
                    0f,                                 // height visual (não usado em BOX)
                    null,                               // modelKey (PRIMITIVE não usa)
                    null,                               // materialKey (ou "Materials/OrcBrute.j3m" se quiser)
                    1.0f                                // scale
            ),
            new PhysicsSpec(BodyKind.CHARACTER, ShapeType.CAPSULE, null,
                    0.60f, 1.80f, 80f, false, false, 0.6f, 0.05f, 0.4f, 60f),
            new AnimationSpec("assets/glb/player/player.glb")
    ),60f),

    ORC_BRUTE(new EntityBlueprint(
            null,null,
            new AnimationSpec("assets/glb/monsters/melee/zombie.glb")
    ),60f),

    SKELETON(new EntityBlueprint(
            null,null,
            new AnimationSpec("assets/glb/monsters/melee/skeleton.glb")
    ),60f),

    WITCH(new EntityBlueprint(
            null,null,
            new AnimationSpec("assets/glb/monsters/melee/witch.glb")
    ),60f),

    DRAGON(new EntityBlueprint(
            null,null,
            new AnimationSpec("assets/glb/monsters/melee/dragon.glb")
    ),60f),

    DROPPED_COIN(new EntityBlueprint(
            new VisualSpec(VisualKind.PRIMITIVE, ShapeType.SPHERE, null,
                    0.15f, 0f, null, null, 1f),
            new PhysicsSpec(BodyKind.GHOST, ShapeType.SPHERE, null,
                    0.75f, 0f, 0f, true, false, 0f, 0f, 0f, 0f), null
    ),0f),
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
                    ), null
            ),0f
    );

    private final EntityBlueprint bp;
    private final float slopLimitDeg; // Quick fix before moving 100% into AnimationSpec.
    // Should probably create a spec for information such as this one.

    EntityType(EntityBlueprint bp, float slopLimitDeg) {
        this.bp = bp;
        this.slopLimitDeg = slopLimitDeg;
    }

    public EntityBlueprint blueprint() {
        return bp;
    }

    public float getSlopLimitDeg() {
        return slopLimitDeg;
    }
}
