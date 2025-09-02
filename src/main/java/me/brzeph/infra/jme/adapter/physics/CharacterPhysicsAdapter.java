package me.brzeph.infra.jme.adapter.physics;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.core.domain.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.brzeph.infra.constants.EnemiesConstants.EPS;

public class CharacterPhysicsAdapter {

    // Constantes úteis
    private static final float DEFAULT_RADIUS = 0.3f;       // raio padrão do BCC
    private static final float DEFAULT_GRAVITY = 10f;     // m/s² (ajuste se quiser)
    private static final float MAX_CLIMB_ANGLE_DEG = 45f;   // rampa máxima “andável”
    private static final float PROBE_HEIGHT = 1.5f;         // de onde lançar o ray para baixo
    private static final float SLIDE_GAIN = 0.6f;           // 0..1 quanto “escorrega” em rampas proibidas

    private final BulletAppState bullet;
    private final Map<String, BetterCharacterControl> controls = new HashMap<>();

    public CharacterPhysicsAdapter(BulletAppState bullet) {
        this.bullet = bullet;
    }

    public void registerCharacter(Character character, Spatial model) {
        if (bullet == null || character == null || model == null) return;

        BetterCharacterControl control = new BetterCharacterControl(
                DEFAULT_RADIUS,
                character.getHeight(),
                character.getWeight()
        );

        // Gravidade e pulo
        control.setGravity(new Vector3f(0f, -DEFAULT_GRAVITY, 0f));
        control.setJumpForce(new Vector3f(0f, character.getJumpForce(), 0f));

        model.addControl(control);

        if (character.getPosition() != null) {
            model.setLocalTranslation(character.getPosition());
        }

        bullet.getPhysicsSpace().add(control);
        controls.put(character.getId(), control);
        model.setUserData("characterId", character.getId()); // userData é para identificar o Spatial.
        bullet.getPhysicsSpace().add(control);
        controls.put(character.getId(), control);
    }

    public void moveCharacter(Character character, Vector3f walkDir) {
        BetterCharacterControl control = controls.get(character.getId());
        if (control == null) return;

        Spatial s = control.getSpatial();
        if (s == null) return;

        // 1) base planar (sua lógica original)
        Vector3f v = (walkDir == null) ? Vector3f.ZERO : walkDir.clone();
        v.y = 0f;

        // 2) ajusta v conforme inclinação do chão
        GroundHit gh = probeGround(s.getWorldTranslation());
        if (gh != null) {
            v = applySlopeRules(character, v, gh.normal, DEFAULT_GRAVITY, MAX_CLIMB_ANGLE_DEG, SLIDE_GAIN);
        }

        control.setWalkDirection(v);

        // 3) olhar/rotação só se houver input tangencial
        if (v.lengthSquared() > EPS) {
            Vector3f lookDir = v.clone().normalizeLocal();
            control.setViewDirection(lookDir);

            Quaternion rot = new Quaternion();
            rot.lookAt(lookDir, Vector3f.UNIT_Y);
            s.setLocalRotation(rot);
            character.setRotation(rot);
        }

        character.setPosition(s.getWorldTranslation().clone());
    }

    public boolean jumpCharacter(Character character) {
        BetterCharacterControl control = controls.get(character.getId());
        if (control != null && control.isOnGround()) {
            // Adicionado check isOnGround "redundante" para retornar boolean para o som
            control.jump();
            return true;
        }
        return false;
    }

    public void removeCharacter(Player player) {
        BetterCharacterControl control = controls.remove(player.getId());
        if (control != null) {
            PhysicsSpace space = bullet.getPhysicsSpace();
            if (space != null) {
                space.remove(control);
            }
            Spatial s = control.getSpatial();
            if (s != null) {
                s.removeControl(control);
            }
        }
    }

    public List<PhysicsRayTestResult> rayTest(Vector3f from, Vector3f to) {
        PhysicsSpace space = bullet.getPhysicsSpace();
        if (space == null) {
            throw new IllegalStateException("PhysicsSpace não está inicializado no BulletAppState!");
        }
        return space.rayTest(from, to);
    }

    private static class GroundHit {
        Vector3f point;
        Vector3f normal;
        float    fraction;
    }

    // Ray para baixo a partir do “peito” do personagem
    private GroundHit probeGround(Vector3f worldPos) {
        Vector3f from = worldPos.add(0, PROBE_HEIGHT, 0);
        Vector3f to   = worldPos.add(0, -PROBE_HEIGHT * 2f, 0);

        List<PhysicsRayTestResult> hits = bullet.getPhysicsSpace().rayTest(from, to);
        if (hits == null || hits.isEmpty()) return null;

        hits.sort(java.util.Comparator.comparingDouble(PhysicsRayTestResult::getHitFraction));
        for (PhysicsRayTestResult r : hits) {
            GroundHit g = new GroundHit();
            g.fraction = r.getHitFraction();
            g.point    = from.interpolateLocal(to, g.fraction).clone();
            // em jME o normal costuma vir em “local”; normalize por segurança
            Vector3f n = r.getHitNormalLocal();
            g.normal = (n != null ? n.normalize() : Vector3f.UNIT_Y.clone());
            return g;
        }
        return null;
    }

    /**
     * Regras de rampa:
     * - Sempre conforma o movimento ao plano (remove componente “dentro da rampa”).
     * - Se ângulo <= max: usa o tangencial normalmente (sobe/ desce).
     * - Se ângulo >  max: bloqueia “subir” e adiciona slide downhill proporcional à gravidade.
     */
    private static Vector3f applySlopeRules(Character character,
                                            Vector3f desiredPlanar,   // já vem com y=0
                                            Vector3f groundNormal,    // pode ter y ≠ 0
                                            float gravityY,
                                            float maxClimbDeg,
                                            float slideGain) {

        float baseSpeed = character.getStats().getSpeed();

        // Sem input: apenas escorrega se for íngreme
        if (desiredPlanar == null || desiredPlanar.lengthSquared() < EPS) {
            return slideOnTooSteepXZ(/*baseTangentXZ=*/Vector3f.ZERO, groundNormal,
                    gravityY, maxClimbDeg, slideGain, baseSpeed);
        }

        // Normal da superfície (apontando para cima)
        Vector3f n = groundNormal.normalize();
        if (n.dot(Vector3f.UNIT_Y) < 0f) n.negateLocal();

        // Ângulo da rampa
        float cos = FastMath.clamp(n.dot(Vector3f.UNIT_Y), -1f, 1f);
        float slopeDeg = FastMath.RAD_TO_DEG * FastMath.acos(cos);

        // Projeta o input no plano da rampa (em 3D) e depois APLANA p/ XZ
        Vector3f vTangent3D = desiredPlanar.subtract(n.mult(desiredPlanar.dot(n))); // v - n*(v·n)
        Vector3f vTangentXZ = new Vector3f(vTangent3D.x, 0f, vTangent3D.z);

        // Se projeção degenerou, volte ao input original
        if (vTangentXZ.lengthSquared() < EPS) {
            vTangentXZ.set(desiredPlanar);
        }

        if (slopeDeg <= maxClimbDeg + 1e-3f) {
            // Rampa válida: mantém a magnitude do input original
            vTangentXZ.normalizeLocal().multLocal(desiredPlanar.length());
            vTangentXZ.y = 0f;
            return vTangentXZ;
        }

        // Rampa proibida: remove componente “subida” no plano XZ
        Vector3f uphill3D = Vector3f.UNIT_Y.subtract(n.mult(Vector3f.UNIT_Y.dot(n))); // projeta +Y no plano
        Vector3f uphillXZ = new Vector3f(uphill3D.x, 0f, uphill3D.z);
        if (uphillXZ.lengthSquared() > EPS) {
            uphillXZ.normalizeLocal();
            float k = vTangentXZ.dot(uphillXZ);
            if (k > 0f) {
                vTangentXZ.subtractLocal(uphillXZ.mult(k)); // bloqueia subir
            }
        }

        // Slide downhill (só XZ): gravidade tangencial projetada e aplanada
        Vector3f g = new Vector3f(0f, -gravityY, 0f);
        Vector3f gTan3D = g.subtract(n.mult(g.dot(n))); // g - n*(g·n)
        Vector3f gTanXZ = new Vector3f(gTan3D.x, 0f, gTan3D.z);
        if (gTanXZ.lengthSquared() > EPS) {
            gTanXZ.normalizeLocal();

            // fator cresce do limite até 90°
            float factor = FastMath.clamp((slopeDeg - maxClimbDeg) / (90f - maxClimbDeg), 0f, 1f);

            float baseline = Math.max(desiredPlanar.length(), baseSpeed);
            float mag = baseline * slideGain * factor;

            vTangentXZ.addLocal(gTanXZ.mult(mag));
        }

        vTangentXZ.y = 0f; // GARANTIA: nunca devolva Y ≠ 0 ao BCC
        return vTangentXZ;
    }
    private static Vector3f slideOnTooSteepXZ(Vector3f baseTangentXZ,
                                              Vector3f groundNormal,
                                              float gravityY,
                                              float maxClimbDeg,
                                              float slideGain,
                                              float defaultSpeed) {
        Vector3f n = groundNormal.normalize();
        if (n.dot(Vector3f.UNIT_Y) < 0f) n.negateLocal();

        float cos = FastMath.clamp(n.dot(Vector3f.UNIT_Y), -1f, 1f);
        float slopeDeg = FastMath.RAD_TO_DEG * FastMath.acos(cos);
        if (slopeDeg <= maxClimbDeg + 1e-3f) {
            // não escorrega se não for “too steep”
            return baseTangentXZ.clone();
        }

        Vector3f g = new Vector3f(0f, -gravityY, 0f);
        Vector3f gTan3D = g.subtract(n.mult(g.dot(n)));
        Vector3f gTanXZ = new Vector3f(gTan3D.x, 0f, gTan3D.z);

        if (gTanXZ.lengthSquared() < EPS) return baseTangentXZ.clone();

        gTanXZ.normalizeLocal();

        float factor = FastMath.clamp((slopeDeg - maxClimbDeg) / (90f - maxClimbDeg), 0f, 1f);
        float baseline = (baseTangentXZ.length() > EPS ? baseTangentXZ.length() : defaultSpeed);
        float mag = baseline * slideGain * factor;

        Vector3f out = baseTangentXZ.add(gTanXZ.mult(mag));
        out.y = 0f;
        return out;
    }

}
