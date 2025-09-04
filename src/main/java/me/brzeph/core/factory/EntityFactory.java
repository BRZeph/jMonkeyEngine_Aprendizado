package me.brzeph.core.factory;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Line;
import com.jme3.util.BufferUtils;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

public class EntityFactory {

    public enum ShapeType { CYLINDER, BOX, SPHERE }

    protected static final float DEFAULT_RADIUS = 0.3f;

    protected final AssetManager assetManager;
    protected final EntityPhysicsAdapter physicsAdapter;

    public EntityFactory(AssetManager assetManager, EntityPhysicsAdapter physicsAdapter) {
        this.assetManager = assetManager;
        this.physicsAdapter = physicsAdapter;
    }

    public Node setupCharacter(Character character, Node root) {
        ShapeType shapeType = getPreferredShapeType(character);
        float radius = getPreferredRadius(character, shapeType);
        float height = character.getHeight();

        Node actor = createBaseNode(character);
        Geometry geom = getCharacterShape(character, shapeType, radius, height);
        actor.attachChild(geom);

        Spatial border = buildEdgeOverlay(assetManager, shapeType, radius, height,
                ColorRGBA.Black, 2f, 32);
        if (border != null) {
            actor.attachChild(border);
        }

        applyMaterial(geom, character);

        root.attachChild(actor);

        getCharacterPhysics(character, actor);

        placeAtSpawn(character, actor, height);

        return actor;
    }

    /* ===================== Hooks de decisão ===================== */

    /** Qual shape usar para o VISUAL (padrão: cilindro). */
    protected ShapeType getPreferredShapeType(Character character) {
        return ShapeType.BOX;
    }

    /** Raio padrão (substitua se tiver largura no Character). */
    protected float getPreferredRadius(Character character, ShapeType shapeType) {
        return DEFAULT_RADIUS;
    }

    /* ===================== Construção do VISUAL ===================== */

    /** Retorna a malha/geometry VISUAL do personagem de acordo com o ShapeType. */
    protected Geometry getCharacterShape(Character character,
                                         ShapeType shapeType,
                                         float radius, float height) {
        Geometry g;
        switch (shapeType) {
            case BOX: {
                Box box = new Box(radius, height * 0.5f, radius);
                g = new Geometry(character.getId() + "_GEOM_BOX", box);
                g.setLocalTranslation(0, height * 0.5f, 0); // apoia no “chão”
                break;
            }
            case SPHERE: {
                float r = Math.max(radius, height * 0.5f); // esfera “cheia”
                Sphere s = new Sphere(16, 24, r);
                g = new Geometry(character.getId() + "_GEOM_SPHERE", s);
                g.setLocalTranslation(0, r, 0);
                break;
            }
            case CYLINDER:
            default: {
                Cylinder mesh = new Cylinder(16, 32, radius, height, true);
                g = new Geometry(character.getId() + "_GEOM_CYL", mesh);
                // Cylinder padrão é Z-up → rotaciona para Y-up e apoia no chão
                g.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
                g.setLocalTranslation(0, height * 0.5f, 0);
                break;
            }
        }
        return g;
    }

    /** Material padrão (substitua conforme seu pipeline). */
    protected void applyMaterial(Geometry geom, Character character) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        if(character instanceof Player){
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.Cyan);
            mat.setColor("Ambient", ColorRGBA.Cyan.mult(0.3f));
            mat.setColor("Specular", ColorRGBA.White);
            mat.setFloat("Shininess", 16f);
        } else {
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.Red);
            mat.setColor("Ambient", ColorRGBA.Red.mult(0.3f));
            mat.setColor("Specular", ColorRGBA.Red);
            mat.setFloat("Shininess", 16f);
        }
        geom.setMaterial(mat);
    }

    /* ===================== Física ===================== */
    protected void getCharacterPhysics(Character character, Node actor) {
        physicsAdapter.registerCharacter(character, actor);
    }

    /* ===================== Posição inicial ===================== */

    /** Crava a posição inicial (usa a do Character se existir; senão, um default). */
    protected void placeAtSpawn(Character character, Node actor, float height) {
        Vector3f spawn = (character.getPosition() != null)
                ? character.getPosition().clone()
                : new Vector3f(5, height * 0.5f + 0.1f, 5);
        actor.setLocalTranslation(spawn);
    }

    /* ===================== Utilidades ===================== */

    protected Node createBaseNode(Character character) {
        return new Node(character.getId());
    }


    /**
     * Cria um overlay de linhas (“borda”) para identificar o shape visual.
     * - BOX: desenha arestas (WireBox).
     * - CYLINDER: desenha 2 aros (topo/base) + 4 linhas verticais.
     * - SPHERE: retorna null (sem bordas).
     *
     * Convenção: o “ator” tem origem no chão (y=0), topo em y=height.
     * Para BOX, centralizamos o WireBox e transladamos +height/2.
     * Para CYLINDER, os aros são gerados em y=0 e y=height.
     */
    protected Spatial buildEdgeOverlay(AssetManager assetManager,
                                       ShapeType type,
                                       float radius, float height,
                                       ColorRGBA lineColor,
                                       float lineWidth,
                                       int circleSamples /* só p/ CYLINDER, ex.: 32 */) {

        switch (type) {
            case BOX: {
                WireBox wb = new WireBox(radius, height * 0.5f, radius);
                Geometry edges = new Geometry("boxEdges", wb);
                Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                m.setColor("Color", lineColor != null ? lineColor : ColorRGBA.Black);
                m.getAdditionalRenderState().setLineWidth(lineWidth <= 0 ? 2f : lineWidth);
                edges.setMaterial(m);

                // centra (WireBox é centrado) e eleva para “sentar” no chão
                edges.setLocalTranslation(0, height * 0.5f, 0);
                return edges;
            }

            case CYLINDER: {
                Node n = new Node("cylEdges");
                Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                m.setColor("Color", lineColor != null ? lineColor : ColorRGBA.Black);
                m.getAdditionalRenderState().setLineWidth(lineWidth <= 0 ? 2f : lineWidth);

                // Aros topo/base
                Geometry top = makeCircleLoop("cylTop", radius, height, circleSamples, m);
                Geometry bot = makeCircleLoop("cylBot", radius, 0f,      circleSamples, m);
                n.attachChild(top);
                n.attachChild(bot);

                // 4 geratrizes verticais (ajuda a “ler” o cilindro)
                n.attachChild(makeLine("cylV1", new Vector3f( radius, 0f, 0f), new Vector3f( radius, height, 0f), m, lineWidth));
                n.attachChild(makeLine("cylV2", new Vector3f(-radius, 0f, 0f), new Vector3f(-radius, height, 0f), m, lineWidth));
                n.attachChild(makeLine("cylV3", new Vector3f(0f, 0f,  radius), new Vector3f(0f,  height,  radius), m, lineWidth));
                n.attachChild(makeLine("cylV4", new Vector3f(0f, 0f, -radius), new Vector3f(0f,  height, -radius), m, lineWidth));

                return n;
            }

            case SPHERE:
            default:
                return null; // sem linhas para esfera
        }
    }

    /* ==================== helpers ==================== */

    private Geometry makeCircleLoop(String name, float radius, float y, int samples, Material sharedMat) {
        int n = Math.max(8, samples); // mínimo razoável
        Vector3f[] pts = new Vector3f[n];
        for (int i = 0; i < n; i++) {
            float a = FastMath.TWO_PI * i / n;
            pts[i] = new Vector3f(FastMath.cos(a) * radius, y, FastMath.sin(a) * radius);
        }
        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.LineLoop);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(pts));
        mesh.updateBound();

        Geometry g = new Geometry(name, mesh);
        g.setMaterial(sharedMat);
        return g;
    }

    private Geometry makeLine(String name, Vector3f a, Vector3f b, Material sharedMat, float width) {
        Line ln = new Line(a, b);
        ln.setLineWidth(width <= 0 ? 2f : width);
        Geometry g = new Geometry(name, ln);
        g.setMaterial(sharedMat);
        return g;
    }
}


