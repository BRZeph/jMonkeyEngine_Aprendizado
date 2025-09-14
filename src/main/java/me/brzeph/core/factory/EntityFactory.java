package me.brzeph.core.factory;

import com.jme3.asset.AssetManager;
import com.jme3.asset.MaterialKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.*;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import me.brzeph.core.domain.entity.CharacterEntity;
import me.brzeph.core.domain.entity.EntityBlueprint;
import me.brzeph.core.domain.entity.GameEntity;
import me.brzeph.core.domain.entity.specs.BodyKind;
import me.brzeph.core.domain.entity.specs.PhysicsSpec;
import me.brzeph.core.domain.entity.specs.VisualKind;
import me.brzeph.core.domain.entity.specs.VisualSpec;

import java.util.Objects;

import static me.brzeph.core.constants.CollisionConstants.*;
import static me.brzeph.core.constants.PhysicsConstants.*;

/** Assumindo que você já tem:
 *  - EntityType com .blueprint() -> EntityBlueprint
 *  - EntityBlueprint.visual() -> VisualSpec
 *  - EntityBlueprint.physics() -> PhysicsSpec
 *  - enums: VisualKind {PRIMITIVE, MODEL}, BodyKind {RIGID, STATIC, CHARACTER, GHOST}, ShapeType {BOX, SPHERE, CAPSULE, HULL, MESH}
 *  - Sua hierarquia GameEntity/Character/Monster/Player/DroppedItem...
 */
public final class EntityFactory {
    private final AssetManager am;
    private final PhysicsSpace physics;

    public EntityFactory(AssetManager am, PhysicsSpace physics) {
        this.am = Objects.requireNonNull(am);
        this.physics = Objects.requireNonNull(physics);
    }

    /**
     * Constrói visual + física para a entidade dada e anexa ao root fornecido.
     */
    public void build(GameEntity e, Node root) {
        EntityBlueprint bp = e.getType().blueprint();
        VisualSpec v = bp.visual();
        PhysicsSpec p = bp.physics();

        // 1) Actor (nó raiz da entidade na cena)
        Node actor = new Node(e.getId());
        actor.setLocalTranslation(e.getSpawnPoint());
        actor.setLocalRotation(e.getSpawnRotation());
        actor.setLocalScale(e.getSpawnScale());

        // 2) Visual
        Spatial visual = buildVisual(v);
        actor.attachChild(visual);

        // 3) Física (importante: para HULL/MESH, derive a shape DEPOIS do visual estar montado e com transform)
        PhysicsControl ctrl = buildPhysics(e, p, actor, visual);
        if (ctrl != null) actor.addControl(ctrl);

        // 4) Link na cena e no espaço de física
        root.attachChild(actor);
        if (ctrl != null) physics.add(actor); // adiciona todos os PhysicsControls do actor

        // 5) (Opcional) guardar referência de volta na entidade
        e.setCharacterNode(actor);

    }

    /* ===================== VISUAL ===================== */

    /** Cria o visual a partir do VisualSpec. Pode retornar Geometry (simples) ou Node (composto). */
    private Spatial buildVisual(VisualSpec v) {
        // MODELO (.gltf/.glb/.j3o etc.)
        if (v.kind() == VisualKind.MODEL) {
            Spatial s = am.loadModel(normalizeKey(v.modelKey()));

            // Se quiser forçar um .j3m por cima do material do modelo
            if (hasKey(v.materialKey())) {
                Material mat = am.loadAsset(new com.jme3.asset.MaterialKey(normalizeKey(v.materialKey())));
                applyMaterialRecursive(s, mat);
            }

            s.scale(v.scale());
            return s;
        }

        // -------- PRIMITIVE --------
        switch (v.shape()) {
            case BOX -> {
                var group = new com.jme3.scene.Node("G-BOX");
                // preenchido
                var fill = new com.jme3.scene.Geometry("G-BOX-FILL",
                        new com.jme3.scene.shape.Box(v.halfExtents().x, v.halfExtents().y, v.halfExtents().z));
                ensureMaterial(fill, v.materialKey());
                group.attachChild(fill);

                // contorno
                var wireBox = new com.jme3.scene.debug.WireBox(v.halfExtents().x, v.halfExtents().y, v.halfExtents().z);
                var outline = new com.jme3.scene.Geometry("G-BOX-OUTLINE", wireBox);
                outline.setMaterial(createWireMaterial());
                outline.setLocalScale(OUTLINE_SCALE_BIAS); // empurra levemente para fora
                group.attachChild(outline);

                // transforma o grupo (escala + “apoia no chão”)
                group.setLocalScale(v.scale());
                group.setLocalTranslation(0f, v.halfExtents().y * v.scale(), 0f);
                return group;
            }

            case SPHERE -> {
                var group = new com.jme3.scene.Node("G-SPHERE");
                // preenchido
                var fill = new com.jme3.scene.Geometry("G-SPHERE-FILL",
                        new com.jme3.scene.shape.Sphere(16, 16, v.radius()));
                ensureMaterial(fill, v.materialKey());
                group.attachChild(fill);

                // contorno
                var wireSphere = new com.jme3.scene.debug.WireSphere(v.radius());
                var outline = new com.jme3.scene.Geometry("G-SPHERE-OUTLINE", wireSphere);
                outline.setMaterial(createWireMaterial());
                outline.setLocalScale(OUTLINE_SCALE_BIAS);
                group.attachChild(outline);

                // escala + apoio no chão
                group.setLocalScale(v.scale());
                group.setLocalTranslation(0f, v.radius() * v.scale(), 0f);
                return group;
            }

            case CAPSULE -> {
                // cápsula composta (cilindro + 2 esferas) já com base em y=0
                var cap = new com.jme3.scene.Node("G-CAPSULE");
                float r = v.radius();
                float h = Math.max(0f, v.height());
                float cylH = Math.max(0f, h - 2f * r);

                // ------- preenchido -------
                var cylMesh = new com.jme3.scene.shape.Cylinder(16, 16, r, Math.max(cylH, 0.0001f), true);
                var gCyl = new com.jme3.scene.Geometry("Capsule-Cyl-FILL", cylMesh);
                gCyl.setLocalTranslation(0, cylH * 0.5f, 0);

                var sTop = new com.jme3.scene.shape.Sphere(16, 16, r);
                var gTop = new com.jme3.scene.Geometry("Capsule-Top-FILL", sTop);
                gTop.setLocalTranslation(0, cylH + r, 0);

                var sBot = new com.jme3.scene.shape.Sphere(16, 16, r);
                var gBot = new com.jme3.scene.Geometry("Capsule-Bot-FILL", sBot);
                gBot.setLocalTranslation(0, 0, 0);

                ensureMaterial(gCyl, v.materialKey());
                ensureMaterial(gTop, v.materialKey());
                ensureMaterial(gBot, v.materialKey());

                cap.attachChild(gCyl);
                cap.attachChild(gTop);
                cap.attachChild(gBot);

                // ------- contorno -------
                var wireMat = createWireMaterial();

                // cilindro (wireframe das faces triangulares)
                var gCylWire = new com.jme3.scene.Geometry("Capsule-Cyl-OUTLINE", cylMesh);
                gCylWire.setMaterial(wireMat);
                gCylWire.setLocalTranslation(0, cylH * 0.5f, 0);
                gCylWire.setLocalScale(OUTLINE_SCALE_BIAS);
                cap.attachChild(gCylWire);

                // esferas em wire
                var topWire = new com.jme3.scene.Geometry("Capsule-Top-OUTLINE", new com.jme3.scene.debug.WireSphere(r));
                topWire.setMaterial(wireMat);
                topWire.setLocalTranslation(0, cylH + r, 0);
                topWire.setLocalScale(OUTLINE_SCALE_BIAS);
                cap.attachChild(topWire);

                var botWire = new com.jme3.scene.Geometry("Capsule-Bot-OUTLINE", new com.jme3.scene.debug.WireSphere(r));
                botWire.setMaterial(wireMat);
                botWire.setLocalTranslation(0, 0, 0);
                botWire.setLocalScale(OUTLINE_SCALE_BIAS);
                cap.attachChild(botWire);

                cap.setLocalScale(v.scale());
                // base já em y=0 (sem offset extra)
                return cap;
            }

            default -> throw new IllegalArgumentException("Primitive não suportado: " + v.shape());
        }
    }

    /* -------------------- Helpers -------------------- */

    private Material createWireMaterial() {
        var m = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", OUTLINE_COLOR);
        // para meshes triangulares (cilindro), força modo wireframe:
        m.getAdditionalRenderState().setWireframe(true);
        m.getAdditionalRenderState().setLineWidth(OUTLINE_LINE_WIDTH);
        // DepthTest padrão mantém as linhas “coladas” ao objeto; OUTLINE_SCALE_BIAS ajuda a evitar z-fight.
        return m;
    }

    private void ensureMaterial(Geometry g, String materialKey) {
        Material m;
        if (hasKey(materialKey)) {
            m = am.loadAsset(new MaterialKey(normalizeKey(materialKey)));
        } else {
            // Fallback ILUMINADO (Lighting.j3md)
            m = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
            m.setBoolean("UseMaterialColors", true);
            m.setColor("Diffuse",  ColorRGBA.Gray);   // cor base
            m.setColor("Specular", ColorRGBA.White);  // brilho
            m.setFloat("Shininess", 32f);                     // 1..128
            m.setColor("Ambient",  ColorRGBA.Gray.mult(0.3f));
        }
        g.setMaterial(m);
    }


    private void applyMaterialRecursive(Spatial s, Material m) {
        if (s instanceof Geometry g) {
            g.setMaterial(m);
        } else if (s instanceof Node n) {
            for (Spatial c : n.getChildren()) applyMaterialRecursive(c, m);
        }
    }

    private static boolean hasKey(String k) {
        return k != null && !k.isEmpty();
    }

    private static String normalizeKey(String key) { // Este método permite utilizar o assetManager.
        if (key == null) return null;
        return key.startsWith("assets/") ? key.substring("assets/".length()) : key;
    }

    /* ===================== FÍSICA ===================== */

    /** Cria o PhysicsControl conforme PhysicsSpec. Usa o actor/visual para gerar HULL/MESH quando necessário. */
    private PhysicsControl buildPhysics(GameEntity e, PhysicsSpec p, Node actor, Spatial visual) {
        switch (p.body().getControlString()) {
            case CONTROL_TYPE_BCC -> {
                BetterCharacterControl bcc = new BetterCharacterControl(p.radius(), p.height(), Math.max(0.0001f, p.mass()));
                if (e instanceof CharacterEntity ch) {
                    float g = Math.abs(WORLD_GRAVITY.y);
                    float H = ch.getMovementStats().getJumpHeight();
                    float v0 = (float) Math.sqrt(2f * g * Math.max(0f, H));
                    float impulseY = p.mass() * v0;
                    bcc.setJumpForce(new Vector3f(0, impulseY, 0));
                } else {
                    // fallback (ex.: NPC sem movement definido)
                    bcc.setJumpForce(new Vector3f(0, p.mass() * 5f, 0));
                }
                return bcc;
            }
            case CONTROL_TYPE_GC -> {
                CollisionShape shape = switch (p.shape()) {
                    case SPHERE -> new SphereCollisionShape(p.radius());
                    case BOX    -> new BoxCollisionShape(p.halfExtents());
                    case CAPSULE-> new CapsuleCollisionShape(p.radius(), Math.max(0f, p.height() - 2f * p.radius()), 1); // eixo Y=1
                    case HULL   -> CollisionShapeFactory.createDynamicMeshShape(visualOrActorForHull(actor, visual));
                    case MESH   -> CollisionShapeFactory.createMeshShape(visualOrActorForHull(actor, visual));
                    default     -> throw new IllegalArgumentException("Ghost shape não suportada: " + p.shape());
                };
                return new GhostControl(shape);
            }
            case CONTROL_TYPE_RBC -> {
                float mass = (p.body() == BodyKind.STATIC) ? 0f : p.mass();
                CollisionShape shape = buildCollisionShape(p, actor, visual, mass > 0f);
                RigidBodyControl rbc = new RigidBodyControl(shape, mass);
                if (p.kinematic() && mass > 0f) rbc.setKinematic(true);
                if (p.friction() > 0f) rbc.setFriction(p.friction());
                rbc.setRestitution(p.restitution());
                if (p.useCCD()) {
                    rbc.setCcdMotionThreshold(0.01f);
                    rbc.setCcdSweptSphereRadius(Math.max(0.001f, p.radius()));
                }
                return rbc;
            }
            default -> { return null; }
        }
    }

    /** Monta a CollisionShape correta. Para HULL/MESH, deriva do visual/actor já com transform. */
    private CollisionShape buildCollisionShape(PhysicsSpec p, Node actor, Spatial visual, boolean dynamic) {
        return switch (p.shape()) {
            case BOX    -> new BoxCollisionShape(p.halfExtents());
            case SPHERE -> new SphereCollisionShape(p.radius());
            case CAPSULE-> new CapsuleCollisionShape(p.radius(), Math.max(0f, p.height() - 2f * p.radius()), 1); // eixo Y
            case HULL   -> CollisionShapeFactory.createDynamicMeshShape(visualOrActorForHull(actor, visual));
            case MESH   -> {
                // MESH é indicado para estáticos; para dinâmicos prefira HULL/convexo
                if (dynamic) {
                    // fallback seguro para dinâmicos: HULL em vez de MESH
                    yield CollisionShapeFactory.createDynamicMeshShape(visualOrActorForHull(actor, visual));
                } else {
                    yield CollisionShapeFactory.createMeshShape(visualOrActorForHull(actor, visual));
                }
            }
            case COMPOUND -> throw new UnsupportedOperationException("Compound requer descrição das peças; modele com EntityBlueprint específico.");
        };
    }

    /** Usa o actor por padrão (já com visual e transforms) para gerar HULL/MESH coerente. */
    private Spatial visualOrActorForHull(Node actor, Spatial visual) {
        // Se o visual carregado for um Node complexo, usar o actor ajuda a pegar a hierarquia toda.
        return actor;
    }
}
