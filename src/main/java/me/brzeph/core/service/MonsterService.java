package me.brzeph.core.service;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;

import static me.brzeph.infra.constants.EnemiesConstants.EPS;

public class MonsterService {

    /** Distância (plana) até o alvo. */
    public static float planarDistance(Vector3f a, Vector3f b) {
        float dx = b.x - a.x, dz = b.z - a.z;
        return FastMath.sqrt(dx*dx + dz*dz);
    }

    /** Vai ultrapassar neste frame (considerando tpf)? */
    public static boolean willOvershoot(Monster m, float tpf) {
        float speed = Math.max(0f, m.getStats().getSpeed());      // m/s
        float dist  = planarDistance(m.getPosition(), m.getWalkingTo());
        float maxStep = speed * Math.max(tpf, 1e-4f);             // metros neste frame
        return dist <= maxStep + 1e-5f;                           // folga numérica
    }

    /** Velocidade (m/s) já CLAMPADA para não ultrapassar o alvo neste frame. */
    public static Vector3f getWalkingVelocity(Monster m, float tpf) {
        float speed = Math.max(0f, m.getStats().getSpeed());      // m/s

        Vector3f cur = m.getPosition();
        Vector3f tgt = m.getWalkingTo();

        Vector3f delta = tgt.subtract(cur);
        delta.y = 0f;
        float dist = delta.length();
        if (dist < EPS) return Vector3f.ZERO;

        // velocidade máxima que chega exatamente no alvo neste frame
        float maxVelThisFrame = dist / Math.max(tpf, 1e-4f);

        float velMag = Math.min(speed, maxVelThisFrame);
        return delta.multLocal(velMag / dist); // direção * velocidade (m/s)
    }

    public static boolean reachedAttackingRange(Monster monster, Character target) {
        float attackRange = monster.getStats().getBasicAttackRange();
        return monster.getPosition().distanceSquared(target.getPosition()) <= attackRange * attackRange;
    }

    /** Alvo alcançado (tolerância pequena, plano) */
    public static boolean reachedWalkingTarget(Monster m) {
        float tol = 0.01f;
        return planarDistance(m.getPosition(), m.getWalkingTo()) <= tol;
    }

    public static boolean isZero(Vector3f v) { // Comparar Vector3f vet != Vector3f.ZERO corretamente.
        return v == null || v.lengthSquared() < 1e-9f;
    }

    public static boolean epsilonEquals(float a, float b, float eps){
        return Math.abs(a - b) <= eps;
    }
}
