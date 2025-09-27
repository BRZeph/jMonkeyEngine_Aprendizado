package me.brzeph.app.systems.impl.collisionSystem.helpers;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import me.brzeph.app.systems.impl.collisionSystem.events.ItemProximityEnter;
import me.brzeph.app.systems.impl.collisionSystem.events.ItemProximityExit;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.domain.entity.GameEntity;
import me.brzeph.events.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class InteractionMatrix {

    private final Map<Long, InteractionRule> rules = new HashMap<>();

    private final CollisionProfiles profiles;

    public InteractionMatrix(EventBus bus){
    /*
    contactStart -> corpos físicos: RBC e BCC + RBC | BCC.
    triggerEnter -> GC + any.
     */
        this.profiles = ServiceLocator.get(CollisionProfiles.class);
        on(CollLayers.PLAYER, CollLayers.ITEM)
                .triggerEnter((player, item) -> bus.post(new ItemProximityEnter(player, item)))
                .triggerExit((player, item)  -> bus.post(new ItemProximityExit(player, item)));

//        on(CollLayers.SPELL, CollLayers.MONSTER)
//                .triggerEnter((spell, monster) -> bus.post(new SpellHit(spell, monster)));
//
//        on(CollLayers.SPELL, CollLayers.PLAYER) // se tiver friendly fire
//                .triggerEnter((spell, entity) -> bus.post(new SpellHit(spell, entity)));

        on(CollLayers.PLAYER, CollLayers.MONSTER)
                .triggerEnter((p, m) -> {
                    System.out.println("Collision!!!");
                })
                .triggerExit((p, m) -> {
                    System.out.println("Collision!!!");
                })
                .contactStart((p, m, ev) -> {
                    System.out.println("Collision!!!");
                })
                .contactEnd((p, m) -> {
                    System.out.println("Collision!!!");
                })
        ;
    }

    public RuleBuilder on(int ga, int gb){
        long k = key(ga, gb);
        InteractionRule rule = rules.computeIfAbsent(k, __ -> new InteractionRule(ga, gb));
        return new RuleBuilder(rule);
    }

    // ----- Chamada pelo CollisionSystem -----
    public void onContactStart(GameEntity a, GameEntity b, PhysicsCollisionEvent ev){
        var rule = rules.get(key(groupOf(a), groupOf(b)));
        if (rule != null) rule.fireContactStart(a, b, ev);
    }
    public void onContactStay(GameEntity a, GameEntity b, PhysicsCollisionEvent ev){
        var rule = rules.get(key(groupOf(a), groupOf(b)));
        if (rule != null) rule.fireContactStay(a, b, ev);
    }
    public void onContactEnd(GameEntity a, GameEntity b){
        var rule = rules.get(key(groupOf(a), groupOf(b)));
        if (rule != null) rule.fireContactEnd(a, b);
    }

    /*
ga (GameEntity A): a entidade já ordenada pelo CollisionSystem para ser o lado A da regra que você configurou. Ex.: se a regra foi criada com on(PLAYER, ITEM), então ga será o Player.
gb (GameEntity B): a entidade já ordenada para ser o lado B da regra. No exemplo acima (on(PLAYER, ITEM)), gb será o Item.
essa ordenação é proposital para que seus handlers sempre recebam os argumentos na mesma semântica da regra (primeiro o grupo A, depois o grupo B), independente de quem o Bullet reportou como “objectA/objectB”.
ghost (GhostControl): o sensor que detectou a sobreposição (a “hitbox” do trigger). É o GhostControl que chamou getOverlappingObjects() naquele frame.
Geralmente ele pertence à entidade que iniciou o teste de proximidade (ex.: o pickup sensor do Player).
Importante: por causa da ordenação ga/gb, o ghost nem sempre é do ga. Ele é “quem detectou”, não “o lado A”. Se você quiser garantir que o dono do ghost é o ga, defina suas regras com o dono do sensor como primeiro grupo (ex.: on(PLAYER, ITEM) quando o ghost está no Player).
other (PhysicsCollisionObject): o objeto físico cru que o ghost encontrou na lista de overlaps naquele instante. Normalmente é o corpo físico de gb (RBC, BCC ou um estático do mundo), mas é passado assim, “bruto”, para você poder:
ler velocidade/massa se for PhysicsRigidBody,
inspecionar getCollisionShape(),
checar/metadados via getUserObject(),
fazer lógica mais fina sem precisar re-resolver o PCO.
     */
    public void onTriggerEnter(GameEntity ga, GameEntity gb, GhostControl ghost, PhysicsCollisionObject other){
        var rule = rules.get(key(groupOf(ga), groupOf(gb)));
        if (rule != null) rule.fireTriggerEnter(ga, gb);
    }
    public void onTriggerStay(GameEntity ga, GameEntity gb, GhostControl ghost, PhysicsCollisionObject other){
        var rule = rules.get(key(groupOf(ga), groupOf(gb)));
        if (rule != null) rule.fireTriggerStay(ga, gb);
    }
    public void onTriggerExit(GameEntity ga, GameEntity gb, GhostControl ghost){
        var rule = rules.get(key(groupOf(ga), groupOf(gb)));
        if (rule != null) rule.fireTriggerExit(ga, gb);
    }

    // --- Helpers ---
    private static long key(int ga, int gb){
        int min = Math.min(ga, gb), max = Math.max(ga, gb);
        return ((long)min << 32) | (long)max;
    }

    private static int groupOf(GameEntity e){
        return ServiceLocator.get(CollisionProfiles.class).groupOf(e);
    }

    // ---- Builder e Rule ----
    public static final class RuleBuilder {
        private final InteractionRule r;
        RuleBuilder(InteractionRule r){ this.r = r; }
        public RuleBuilder contactStart(TriConsumer<GameEntity,GameEntity, PhysicsCollisionEvent> c){ r.onContactStart = c; return this; }
        public RuleBuilder contactStay (TriConsumer<GameEntity,GameEntity, PhysicsCollisionEvent> c){ r.onContactStay  = c; return this; }
        public RuleBuilder contactEnd  (BiConsumer<GameEntity,GameEntity> c){ r.onContactEnd = c; return this; }
        public RuleBuilder triggerEnter(BiConsumer<GameEntity,GameEntity> c){ r.onTriggerEnter = c; return this; }
        public RuleBuilder triggerStay (BiConsumer<GameEntity,GameEntity> c){ r.onTriggerStay  = c; return this; }
        public RuleBuilder triggerExit (BiConsumer<GameEntity,GameEntity> c){ r.onTriggerExit  = c; return this; }
    }

    public static final class InteractionRule {
        final int ga, gb;
        TriConsumer<GameEntity,GameEntity,PhysicsCollisionEvent> onContactStart, onContactStay;
        BiConsumer<GameEntity,GameEntity> onContactEnd, onTriggerEnter, onTriggerStay, onTriggerExit;
        InteractionRule(int ga, int gb){ this.ga = ga; this.gb = gb; }
        void fireContactStart(GameEntity a, GameEntity b, PhysicsCollisionEvent e){ if(onContactStart!=null) onContactStart.accept(order(a,b), orderB(a,b), e); }
        void fireContactStay (GameEntity a, GameEntity b, PhysicsCollisionEvent e){ if(onContactStay !=null) onContactStay .accept(order(a,b), orderB(a,b), e); }
        void fireContactEnd  (GameEntity a, GameEntity b){ if(onContactEnd  !=null) onContactEnd .accept(order(a,b), orderB(a,b)); }
        void fireTriggerEnter(GameEntity a, GameEntity b){ if(onTriggerEnter!=null) onTriggerEnter.accept(order(a,b), orderB(a,b)); }
        void fireTriggerStay (GameEntity a, GameEntity b){ if(onTriggerStay !=null) onTriggerStay .accept(order(a,b), orderB(a,b)); }
        void fireTriggerExit (GameEntity a, GameEntity b){ if(onTriggerExit !=null) onTriggerExit .accept(order(a,b), orderB(a,b)); }

        // Mantém a semântica A×B independente da ordem do evento
        private GameEntity order(GameEntity a, GameEntity b){ return (groupOf(a) == ga) ? a : b; }
        private GameEntity orderB(GameEntity a, GameEntity b){ return (groupOf(a) == ga) ? b : a; }
    }

    // Interfaces utilitárias
    @FunctionalInterface public interface TriConsumer<A,B,C>{ void accept(A a, B b, C c); }
}

