package me.brzeph.infra.jme.adapter.physics;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import me.brzeph.domain.entity.CharacterEntity;

import java.util.List;

import static me.brzeph.constants.EnemiesConstants.EPS;
import static me.brzeph.constants.PhysicsConstants.G;
import static me.brzeph.constants.PhysicsConstants.WORLD_GRAVITY;

public class EntityPhysicsAdapter {

    private static final float PROBE_HEIGHT = 1.5f;         // de onde lançar o ray para baixo
    private static final float SLIDE_GAIN = 0.9f;           // 0..1 quanto “escorrega” em rampas proibidas

    private final BulletAppState bullet;

    public EntityPhysicsAdapter(BulletAppState bullet) {
        this.bullet = bullet;
    }

    public void moveCharacter(CharacterEntity e, Vector3f wishDirWorld) {
        RigidBodyControl rbc = (RigidBodyControl) e.getControl();
        if (rbc == null) return;
        if (wishDirWorld == Vector3f.ZERO){
            rbc.setAngularFactor(Vector3f.ZERO);
            rbc.setDamping(0.1f, 0.95f);           // muito damping angular
            rbc.setFriction(1.5f);
            rbc.setSleepingThresholds(0.2f, 0.2f); // deixa dormir fácil
            return;
        }

        // mantenha o corpo “em pé”
        rbc.setAngularFactor(new Vector3f(0, 1, 0)); // gira só no Y (ou ZERO p/ travar tudo)

        Vector3f wish = (wishDirWorld == null ? Vector3f.ZERO : wishDirWorld.clone());
        wish.y = 0f;

        // projeção na rampa (se tiver sua regra de declive)
        GroundHit gh = probeGround(rbc.getPhysicsLocation());
        if (gh != null) {
            wish = applySlopeRules(e, wish, gh.normal, WORLD_GRAVITY.y, SLIDE_GAIN);
        }

        // velocidade alvo no plano
        Vector3f targetXZ = wish.normalizeLocal().multLocal(e.getStats().getSpeed());
        Vector3f vel = rbc.getLinearVelocity();
        vel.x = targetXZ.x;
        vel.z = targetXZ.z;
        rbc.setLinearVelocity(vel); // física cuida do Y

        // orientação (yaw) para onde está andando
        if (targetXZ.lengthSquared() > 1e-6f) {
            Quaternion yaw = lookYawFromForward(targetXZ);
            rbc.setPhysicsRotation(yaw);
            e.setRotation(yaw);
        }

        e.setPosition(rbc.getPhysicsLocation().clone());
    }

    private static Quaternion lookYawFromForward(Vector3f fwdWorld) {
        Vector3f f = fwdWorld.clone().setY(0).normalizeLocal();
        Matrix3f m = new Matrix3f();
        m.fromAxes(
                f.cross(Vector3f.UNIT_Y).normalizeLocal(), // right
                Vector3f.UNIT_Y,                            // up
                f.negateLocal()                             // back
        );
        return new Quaternion().fromRotationMatrix(
                m
        );
    }

    public boolean jumpCharacter(CharacterEntity characterEntity) {
//        BetterCharacterControl control = (BetterCharacterControl) characterEntity.getControl();
//        if (control != null && control.isOnGround()) {
//            control.jump();
//            return true;
//        }
//        return false;
        return false;
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
    private static Vector3f applySlopeRules(CharacterEntity characterEntity,
                                            Vector3f desiredPlanar,   // já vem com y=0
                                            Vector3f groundNormal,    // pode ter y ≠ 0
                                            float gravityY,
                                            float slideGain) {
        float maxClimbDeg = characterEntity.getType().blueprint().physics() == null ?
                characterEntity.getType().getSlopLimitDeg() :
                characterEntity.getType().blueprint().physics().slopeLimitDeg();

        float baseSpeed = characterEntity.getStats().getSpeed();

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
