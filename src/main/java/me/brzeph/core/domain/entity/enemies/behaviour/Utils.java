package me.brzeph.core.domain.entity.enemies.behaviour;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.core.domain.util.RandomUtils;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;

import java.util.List;

public class Utils {
    public static boolean canSee(Character source, Character target,
                                 float maxDistance, float fovDeg) {
        boolean debug = false;

        if (source == null || target == null || !source.isAlive() || !target.isAlive()) return false;

        // “olhos” (pode só planarizar se preferir)
        Vector3f sourcePos = source.getPosition().add(0, source.getHeight() * 0.8f, 0);
        Vector3f targetPos = target.getPosition().add(0, target.getHeight() * 0.8f, 0);

        // Distância
        Vector3f toTarget = targetPos.subtract(sourcePos);
        float distance = toTarget.length();
        if (distance > maxDistance) return false;

        // FOV no plano (ignora diferença de Y para visão horizontal mais estável)
        Vector3f forward = source.getRotation().mult(Vector3f.UNIT_Z);
        forward.y = 0; forward.normalizeLocal();
        Vector3f toTargetPlanar = new Vector3f(toTarget.x, 0, toTarget.z).normalizeLocal();
        float angle = forward.angleBetween(toTargetPlanar);
        if (angle > FastMath.DEG_TO_RAD * (fovDeg * 0.5f)) return false;

        // Raycast físico
        CharacterPhysicsAdapter adapter = ServiceLocator.get(CharacterPhysicsAdapter.class);
        List<PhysicsRayTestResult> results = adapter.rayTest(sourcePos, targetPos);

        // Nada bateu → caminho livre
        if (results == null || results.isEmpty()) return true;

        // Ordena do mais perto para o mais longe (por garantia)
        results.sort(java.util.Comparator.comparingDouble(PhysicsRayTestResult::getHitFraction));

        if(debug) System.out.println(
                "[SEE] src=" + source.getName() + " → tgt=" + target.getName()
                + " dist=" + distance
                + " angleDeg=" + Math.toDegrees(angle)
                + " hits=" + results.size()
        );

        for (PhysicsRayTestResult r : results) {
            Object uo = r.getCollisionObject().getUserObject();

            // Ignora a si mesmo
            if (uo == source) continue;

            // Acertou o alvo → visão livre
            if (uo == target) return true;

            if (uo instanceof Spatial) {
                Spatial s = (Spatial) uo;
                String id = s.getUserData("characterId"); // userData é para identificar o Spatial.
                if (id != null && id.equals(target.getId())) return true;
                // espaço para tratar terreno/parede via outra userData (ex.: "isObstacle")
                return false; // outro spatial bloqueia
            }

            // Qualquer outra coisa com userObject → bloqueou
            if (uo != null) return false;

            // Se uo == null, costuma ser terreno/props sem marcação → trate como bloqueio.
            return false;
        }
        throw new RuntimeException("Could not find result for rayTracing:\n[SEE] src=" + source.getName() + " tgt=" + target.getName());
    }


    /** Tenta achar um ponto válido ao redor. Retorna null se não conseguir em maxTries. */
    public static Vector3f pickRandomWalkableAround(
            Vector3f origin,
            float maxDst,
            float maxSlopeDeg,
            int maxTries,
            float probeUp, float probeDown,
            Character self // para filtrar self nos raycasts
    ) {
        CharacterPhysicsAdapter adapter = ServiceLocator.get(CharacterPhysicsAdapter.class);

        for (int i = 0; i < maxTries; i++) {
            Vector3f candidateXZ = RandomUtils.getRandomIdlePosition(origin, maxDst);

            // 1) Ray para baixo: encontrar o "chão" e sua normal
            GroundHit gh = raycastGround(candidateXZ, origin.y, probeUp, probeDown, adapter);
            if (gh == null) continue;

            // 2) Checar inclinação (walkable)
            if (!isSlopeWalkable(gh.normal, maxSlopeDeg)) continue;

            // 3) (Opcional) Checar caminho até o ponto (parede/obstáculo no meio)
            if (!clearPathXZ(origin, gh.point, adapter, self)) continue;

            return gh.point; // ponto válido
        }
        return null;
    }

    /* ---------- helpers ---------- */

    public static class GroundHit {
        public final Vector3f point;
        public final Vector3f normal;
        public GroundHit(Vector3f p, Vector3f n){ this.point=p; this.normal=n; }
    }

    /** Ray para baixo em XZ do candidato, partindo de originY+probeUp até originY-probeDown. */
    public static GroundHit raycastGround(Vector3f candidateXZ, float originY,
                                          float probeUp, float probeDown,
                                          CharacterPhysicsAdapter adapter) {
        Vector3f from = new Vector3f(candidateXZ.x, originY + probeUp, candidateXZ.z);
        Vector3f to   = new Vector3f(candidateXZ.x, originY - probeDown, candidateXZ.z);

        List<PhysicsRayTestResult> hits = adapter.rayTest(from, to);
        if (hits == null || hits.isEmpty()) return null;

        hits.sort(java.util.Comparator.comparingDouble(PhysicsRayTestResult::getHitFraction));
        PhysicsRayTestResult hit = hits.get(0);

        float f = hit.getHitFraction();
        Vector3f point = from.add(to.subtract(from).mult(f)); // interpola
        Vector3f n = hit.getHitNormalLocal();
        if (n == null) n = Vector3f.UNIT_Y.clone(); else n = n.normalize();

        return new GroundHit(point, n);
    }

    public static boolean isSlopeWalkable(Vector3f normal, float maxSlopeDeg) {
        Vector3f n = normal.normalize();
        float cos = FastMath.clamp(n.dot(Vector3f.UNIT_Y), -1f, 1f);
        float slopeDeg = FastMath.RAD_TO_DEG * FastMath.acos(cos); // 0=plano, 90=parede
        return slopeDeg <= maxSlopeDeg + 1e-3f;
    }

    /** Ray em linha reta no plano (ombro) para ver se há parede entre origem e destino. */
    public static boolean clearPathXZ(Vector3f fromPos, Vector3f toPos,
                                      CharacterPhysicsAdapter adapter, Character self) {
        final float SHOULDER = 1.0f; // altura do “ombro” para evitar chão
        Vector3f a = new Vector3f(fromPos.x, fromPos.y + SHOULDER, fromPos.z);
        Vector3f b = new Vector3f(toPos.x,   toPos.y   + SHOULDER, toPos.z);

        List<PhysicsRayTestResult> hits = adapter.rayTest(a, b);
        if (hits == null || hits.isEmpty()) return true;

        hits.sort(java.util.Comparator.comparingDouble(PhysicsRayTestResult::getHitFraction));
        for (PhysicsRayTestResult r : hits) {
            Object uo = r.getCollisionObject().getUserObject();

            // ignore self
            if (uo == self) continue;

            // se marcou o terreno, ignore (veja nota abaixo)
            if ("terrain".equals(uo)) continue;

            // se for um Spatial do próprio terreno e você marcou com userData, pode checar aqui

            // qualquer outra coisa no caminho bloqueia
            return false;
        }
        return true;
    }
}
