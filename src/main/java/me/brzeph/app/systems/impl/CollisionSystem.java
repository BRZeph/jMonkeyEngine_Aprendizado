package me.brzeph.app.systems.impl;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.scene.Spatial;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.infra.events.EventBus;

public class CollisionSystem extends SystemAbs implements PhysicsCollisionListener, PhysicsTickListener {

    private final BulletAppState bullet;
    private final EventBus bus;

    public CollisionSystem() {
        this.bullet = getBullet();
        this.bus = getBus();
    }

    /* ---------- ciclo de vida ---------- */

    public void initialize() {
        PhysicsSpace ps = bullet.getPhysicsSpace();
        ps.addCollisionListener(this);
        ps.addTickListener(this);
    }

    /* ---------- listener de colisões rígidas (thread da física) ---------- */

    @Override
    public void collision(PhysicsCollisionEvent ev) {
        PhysicsCollisionObject aObj = ev.getObjectA();
        PhysicsCollisionObject bObj = ev.getObjectB();

        boolean aGhost = isGhost(aObj);
        boolean bGhost = isGhost(bObj);

        // CASO 1: envolve Ghost (tratamento de "GhostCollision")
        if (aGhost || bGhost) {
            if (aGhost && bGhost) return;

            PhysicsCollisionObject ghostObj = aGhost ? aObj : bObj;
            PhysicsCollisionObject otherObj = aGhost ? bObj : aObj;
            handleGhostCollision(ghostObj, otherObj, ev);
            return;
        }

        // CASO 2: colisões físicas "normais"
        handleRigidCollision(aObj, bObj, ev);
    }

    /* --------- ghost --------- */
    private void handleGhostCollision(PhysicsCollisionObject ghostObj,
                                      PhysicsCollisionObject otherObj,
                                      PhysicsCollisionEvent ev) {
        Spatial sensor = toSpatial(ghostObj);
        Spatial other  = toSpatial(otherObj);
        if (sensor == null || other == null) return;
        ((ChatSystem) getSystem(ChatSystem.class)).send(ChatChannel.GLOBAL, "", "Ghost collision: " + sensor.getName() + " | " + other.getName());

        // Tratar aqui
    }

    private static boolean isGhost(PhysicsCollisionObject pco) {
        if (pco == null) return false;
        if (pco instanceof PhysicsGhostObject) return true;
        return "PhysicsGhostObject".equals(pco.getClass().getSimpleName());
    }

    /* --------- rígido --------- */
    private void handleRigidCollision(PhysicsCollisionObject aObj,
                                      PhysicsCollisionObject bObj,
                                      PhysicsCollisionEvent ev) {
        // tente pegar direto do evento; se vier null, usa fallback no userObject
        Spatial a = ev.getNodeA(); if (a == null) a = toSpatial(aObj);
        Spatial b = ev.getNodeB(); if (b == null) b = toSpatial(bObj);
        if (a == null || b == null) return;
        ((ChatSystem) getSystem(ChatSystem.class)).send(ChatChannel.GLOBAL, "", "Rigid collision: " + a.getName() + " | " + b.getName());

        // Tratar aqui
    }

    private static Spatial toSpatial(PhysicsCollisionObject pco) {
        Object uo = pco.getUserObject();
        return (uo instanceof Spatial) ? (Spatial) uo : null;
    }
    /* ---------- tick da física: varredura de Ghosts (thread da física) ---------- */

    @Override
    public void physicsTick(PhysicsSpace space, float f) {

    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float f) {

    }

    /* ---------- loop do app: postar no EventBus ---------- */

    @Override
    public void update(float tpf) {

    }
}
