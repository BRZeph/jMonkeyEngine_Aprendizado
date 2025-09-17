package me.brzeph.core.domain.entity.enemies.behaviour;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import me.brzeph.app.systems.impl.collisionSystem.helpers.CollLayers;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.domain.entity.CharacterEntity;
import me.brzeph.core.domain.entity.GameEntity;
import me.brzeph.core.domain.util.RandomUtils;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import java.util.List;

public class Utils {
    public static boolean canSee(CharacterEntity source, CharacterEntity target,
                                 float maxDistance, float fovDeg) {
        if (source == null || target == null || !source.isAlive() || !target.isAlive()) return false;

        // Olhos
        Vector3f sourcePos = source.getPosition().add(0, source.getHeight() * 0.8f, 0);
        Vector3f targetPos = target.getPosition().add(0, target.getHeight() * 0.8f, 0);

        // Distância
        Vector3f toTarget = targetPos.subtract(sourcePos);
        float distance = toTarget.length();
        if (distance > maxDistance) return false;

        // FOV (plano)
        Vector3f forward = source.getRotation().mult(Vector3f.UNIT_Z);
        forward.y = 0; forward.normalizeLocal();
        Vector3f toTargetPlanar = new Vector3f(toTarget.x, 0, toTarget.z).normalizeLocal();
        float angle = forward.angleBetween(toTargetPlanar);
        if (angle > FastMath.DEG_TO_RAD * (fovDeg * 0.5f)) return false;

        // Origem levemente à frente pra evitar auto-acerto
        Vector3f from = sourcePos.add(forward.mult(0.05f));

        // Raycast
        EntityPhysicsAdapter adapter = ServiceLocator.get(EntityPhysicsAdapter.class);
        List<PhysicsRayTestResult> results = adapter.rayTest(from, targetPos);
        if (results == null || results.isEmpty()) return true;

        results.sort(java.util.Comparator.comparingDouble(PhysicsRayTestResult::getHitFraction));

        for (PhysicsRayTestResult r : results) {
            PhysicsCollisionObject pco = r.getCollisionObject();
            Object uo = pco.getUserObject();

            // 1) ignore self (corpo e ghosts do próprio source)
            if (uo == source) continue;

            // 2) ignore GHOSTS sempre
            if (pco instanceof com.jme3.bullet.objects.PhysicsGhostObject) continue;

            // 3) alvo?
            if (uo == target) return true;
            if (uo instanceof GameEntity ge && ge.getId().equals(target.getId())) return true;

            // 4) grupos que NÃO bloqueiam visão (ajuste a seu gosto)
            int g = pco.getCollisionGroup();
            if ((g & (CollLayers.ITEM | CollLayers.SPELL | CollLayers.SENSOR)) != 0) {
                // itens, hitboxes, sensores não bloqueiam LoS
                continue;
            }

            // 5) obstáculos que BLOQUEIAM
            // mundo/estático
            if ((g & CollLayers.WORLD) != 0) return false;
            if (pco instanceof com.jme3.bullet.objects.PhysicsRigidBody rb && rb.getMass() == 0f) return false;

            // 6) (opcional) outros personagens bloqueiam
            if ((g & (CollLayers.PLAYER | CollLayers.MONSTER | CollLayers.NPC)) != 0) return false;

            // Se chegou aqui e não decidiu, apenas continue procurando o alvo
        }

        // Não achou o alvo antes de um bloqueio
        return false;
    }

    /** Tenta achar um ponto válido ao redor. Retorna null se não conseguir em maxTries. */
    public static Vector3f pickRandomWalkableAround(
            Vector3f origin,
            float maxDst,
            float maxSlopeDeg,
            int maxTries,
            float probeUp, float probeDown,
            CharacterEntity self // para filtrar self nos raycasts
    ) {
        EntityPhysicsAdapter adapter = ServiceLocator.get(EntityPhysicsAdapter.class);

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
                                          EntityPhysicsAdapter adapter) {
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
                                      EntityPhysicsAdapter adapter, CharacterEntity self) {
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
