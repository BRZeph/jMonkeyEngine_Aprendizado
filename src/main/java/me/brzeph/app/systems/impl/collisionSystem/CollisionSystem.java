package me.brzeph.app.systems.impl.collisionSystem;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.ItemSystem;
import me.brzeph.app.systems.impl.collisionSystem.helpers.CollisionProfile;
import me.brzeph.app.systems.impl.collisionSystem.helpers.CollisionProfiles;
import me.brzeph.app.systems.impl.collisionSystem.helpers.InteractionMatrix;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.domain.entity.GameEntity;
import me.brzeph.domain.entity.enemies.Monster;
import me.brzeph.domain.entity.item.DroppedItem;
import me.brzeph.domain.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.brzeph.app.systems.impl.collisionSystem.helpers.CollLayers.*;

public final class CollisionSystem extends SystemAbs
        implements PhysicsCollisionListener, PhysicsTickListener {

    private final PhysicsSpace space;
    private final CollisionProfiles profiles;
    // Mapa de PCO -> entidade (funciona p/ RBC, GC e BCC)
    private final Map<PhysicsCollisionObject, GameEntity> byPco = new HashMap<>();
    // Para Ghosts: histórico de overlaps por ghost
    private final Map<GhostControl, Map<PhysicsCollisionObject, GameEntity>> ghostPrev = new HashMap<>();
    // Pares ativos (contato físico)
    private final Set<Long> activePairs = new HashSet<>();
    private final Set<Long> pairsSeenThisTick = new HashSet<>();
    private final Map<Long, EntityPair> pairIndex = new HashMap<>();
    // Fila thread-safe do physics -> update
    private final Queue<Runnable> deferred = new ConcurrentLinkedQueue<>();

    // Regras
    private final InteractionMatrix rules;

    public CollisionSystem(){
        this.space = ServiceLocator.get(PhysicsSpace.class);
        this.profiles = initProfiles();
        ServiceLocator.put(CollisionProfiles.class, profiles);
        rules = new InteractionMatrix(getBus());
        space.addCollisionListener(this);
        space.addTickListener(this);
    }

    private CollisionProfiles initProfiles() {
        return new CollisionProfiles()
                .map(Player.class,      new CollisionProfile(PLAYER,  WORLD | MONSTER | PLAYER | ITEM))
                .map(Monster.class,     new CollisionProfile(MONSTER, WORLD | MONSTER | PLAYER))
                .map(DroppedItem.class, new CollisionProfile(ITEM,    WORLD | PLAYER))
                // .map(GameSpell.class,   new CollisionProfile(SPELL,   WORLD | MONSTER | PLAYER))
                ;
    }

    // -------- Registro / Anexação --------

    public static void attach(GameEntity e){
        CollisionSystem collisionSystem = getSystem(CollisionSystem.class);
        PhysicsControl ctrl =  e.getControl();
        CollisionProfile prof = collisionSystem.profiles.forEntity(e);
        collisionSystem.applyGroups(ctrl, prof);
        if (ctrl.getPhysicsSpace() == null) collisionSystem.space.add(ctrl);
        collisionSystem.mapEntityToControl(e, ctrl);
    }

    public void deSpawn(GameEntity e){
        PhysicsControl ctrl = e.getControl();
        unregisterLookup(ctrl);
        if (ctrl instanceof GhostControl gc) ghostPrev.remove(gc);
        space.remove(ctrl);
        getSystem(ItemSystem.class).deSpawn((DroppedItem)e);
    }

    // -------- PhysicsCollisionListener --------
    @Override
    public void collision(PhysicsCollisionEvent event) { // RBC / BCC
        PhysicsCollisionObject a = event.getObjectA();
        PhysicsCollisionObject b = event.getObjectB();
        GameEntity ea = byPco.get(a), eb = byPco.get(b);
        if (ea == null || eb == null) return;

        long key = pairKey(ea, eb);
        if (!activePairs.contains(key)) {
            activePairs.add(key);
            deferred.add(() -> rules.onContactStart(ea, eb, event));
        } else {
            deferred.add(() -> rules.onContactStay(ea, eb, event));
        }
    }

    // -------- PhysicsTickListener --------
    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) { // Ghosts
        for (var entry : ghostPrev.entrySet()) {
            GhostControl gc = entry.getKey();
            Map<PhysicsCollisionObject, GameEntity> prev = entry.getValue();

            Map<PhysicsCollisionObject, GameEntity> now = new HashMap<>();
            GameEntity egc = byPco.get(gc); // entidade dona do ghost

            for (PhysicsCollisionObject o : gc.getOverlappingObjects()) {
                GameEntity eo = byPco.get(o);
                if (eo == null) continue;

                now.put(o, eo);
                if (!prev.containsKey(o)) {
                    deferred.add(() -> rules.onTriggerEnter(egc, eo, gc, o));
                } else {
                    deferred.add(() -> rules.onTriggerStay(egc, eo, gc, o));
                }
            }

            // EXITS
            for (var prevObj : prev.keySet()) {
                if (!now.containsKey(prevObj)) {
                    GameEntity eo = prev.get(prevObj);
                    deferred.add(() -> rules.onTriggerExit(egc, eo, gc));
                }
            }

            ghostPrev.put(gc, now);
        }
    }

    @Override public void physicsTick(PhysicsSpace space, float tpf) {
    }

    // Chamar no update() do app (thread principal)
    public void pump(){
        Runnable r;
        while ((r = deferred.poll()) != null) r.run();
    }

    // -------- Utilitários --------
    private void applyGroups(PhysicsControl ctrl, CollisionProfile prof){
        int g = prof.group(), m = prof.mask();
        if (ctrl instanceof RigidBodyControl rbc && rbc.getMass() != 0){
            rbc.setCollisionGroup(g);
            rbc.setCollideWithGroups(m);
        } else if (ctrl instanceof GhostControl gc){
            gc.setCollisionGroup(g);
            gc.setCollideWithGroups(m);
            ghostPrev.putIfAbsent(gc, new HashMap<>());
        } else if (ctrl instanceof BetterCharacterControl bcc){
            PhysicsRigidBody rb = bcc.getRigidBody(); // JME 3.8.1
            rb.setCollisionGroup(g);
            rb.setCollideWithGroups(m);
        } else {
            throw new IllegalArgumentException("PhysicsControl não suportado: " + ctrl.getClass());
        }
    }

    private void mapEntityToControl(GameEntity e, PhysicsControl ctrl){
        if (ctrl instanceof RigidBodyControl rbc){
            rbc.setUserObject(e);
            byPco.put(rbc, e);
        } else if (ctrl instanceof GhostControl gc){
            gc.setUserObject(e);
            byPco.put(gc, e);
        } else if (ctrl instanceof BetterCharacterControl bcc){
            PhysicsRigidBody rb = bcc.getRigidBody();
            rb.setUserObject(e);
            byPco.put(rb, e);
        }
    }

    private void unregisterLookup(PhysicsControl ctrl){
        PhysicsCollisionObject pco = toPCO(ctrl);
        byPco.remove(pco);
    }

    private PhysicsCollisionObject toPCO(PhysicsControl ctrl){
        if (ctrl instanceof RigidBodyControl rbc) return rbc;
        if (ctrl instanceof GhostControl gc) return gc;
        if (ctrl instanceof BetterCharacterControl bcc) return bcc.getRigidBody();
        throw new IllegalArgumentException("PCO não encontrado para " + ctrl.getClass());
    }

    private static long pairKey(GameEntity a, GameEntity b){
        String ia = a.getId(), ib = b.getId();
        int ha = ia.hashCode(), hb = ib.hashCode();
        int min = Math.min(ha, hb), max = Math.max(ha, hb);
        return ((long)min & 0xFFFFFFFFL) << 32 | ((long)max & 0xFFFFFFFFL);
    }

    @Override
    public void update(float tpf) {
    }

    public record EntityPair(GameEntity a, GameEntity b) {}

}
