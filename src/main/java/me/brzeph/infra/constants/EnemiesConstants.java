package me.brzeph.infra.constants;

import com.jme3.math.Vector3f;
import me.brzeph.app.systems.impl.PlayerSystem;
import me.brzeph.bootstrap.ServiceLocator;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.MonsterBehaviour;
import me.brzeph.core.domain.entity.enemies.behaviour.*;
import me.brzeph.core.domain.entity.enemies.behaviour.node.*;
import me.brzeph.core.service.MonsterService;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.entities.enemies.MonsterAggroEvent;
import me.brzeph.infra.events.entities.enemies.MonsterWalkEvent;

import java.util.HashMap;

import java.util.List;

public class EnemiesConstants {
    public static final float GOBLIN_BASE_SPEED = 3f; // Refatorar isso de forma melhor usando enum.
    public static final float MAGE_BASE_SPEED = 2f;
    public static final float WITCH_BASE_SPEED = 2.5f;
    public static final float SKELETON_BASE_SPEED = 4f;
    public static final float ARCHER_BASE_SPEED = 1.8f;
    public static final float GUNSLINGER_BASE_SPEED = 3.3f;

    public static final float EPS = 1e-6f;

    public static final HashMap<MonsterBehaviour, BehaviourTree> BEHAVIOUR_TREE_MAP = new HashMap<>();

    public static final BehaviourTree CASTER_BEHAVIOUR_TREE = createCasterBehaviourTree();
    public static final BehaviourTree MELEE_BEHAVIOUR_TREE = createMeleeBehaviourTree();

    static {
        BEHAVIOUR_TREE_MAP.put(MonsterBehaviour.CASTER, CASTER_BEHAVIOUR_TREE);
        BEHAVIOUR_TREE_MAP.put(MonsterBehaviour.MELEE, MELEE_BEHAVIOUR_TREE);
    }

    /*
        ************* READ ME BEFORE CHANGING TREE *************
        * This code *SHOULD* be event oriented, instead of moving the monster here, raise event and have the
        * MonsterSystem.class handle it.
     */

    private static BehaviourTree createCasterBehaviourTree() {
        return null;
    }

    private static BehaviourTree createMeleeBehaviourTree() {
        /*
SelectorNode (root)
│
├── SequenceNode (Aggro == null)
│   │
│   └── SelectorNode
│       │
│       ├── SequenceNode (canSeeTarget)
│       │   └── ActionNode: post(MonsterAggroEvent) + walkingTo = null
│       │
│       ├── SequenceNode (walkingTo == null)
│       │   └── ActionNode: post(MonsterIdleWalkToEvent) → posição aleatória
│       │
│       └── SequenceNode (walkingTo != null)
│           └── ActionNode: post(MonsterWalkEvent) → andar até walkingTo
│
├── SequenceNode (Aggro != null)
│   └── ActionNode: post(MonsterWalkEvent) → andar até aggro.getPosition()
│
└── ActionNode (Fallback / Idle)
    └── post(MonsterIdleEvent)

         */
        EventBus bus = ServiceLocator.get(EventBus.class);
        List<Player> playerList = List.of( // Deixando como lista para facilitar multiplayer.
                ((PlayerSystem)me.brzeph.app.systems.System.getSystem(PlayerSystem.class)).getPlayer()
        );
        boolean debug = false;
        Node root = new SelectorNode(List.of(

                // 1️⃣ Se aggro é null → verificar se consegue ver o Player
                new SequenceNode(List.of(
                        new ConditionNode(monster -> monster.getAggro() == null), // aggro vazio
                        new SelectorNode(List.of(

                                // 1a. Consegue ver o player
                                new SequenceNode(List.of(
                                        new ConditionNode(monster -> {
//                                            boolean canSee = monster.canSeeTarget(playerList) != null;
                                            boolean canSee = false; // Desabilitado aggro no player para testar movimentação de monstros.
                                            if(debug) System.out.println("[Condition] Monster " + monster.getName() + " canSeeTarget? " + canSee);
                                            return canSee;
                                        }),
                                        new ActionNode(monster -> {
                                            Player player = (Player) monster.canSeeTarget(playerList);
                                            if(debug) System.out.println("[Action] Monster " + monster.getName() + " aggro on player " + player.getName());
                                            bus.post(new MonsterAggroEvent(monster, player));
                                        })
                                )),

                                // 1b. Não consegue ver o player → walkingTo vazio? gera alvo aleatório
                                new SequenceNode(List.of(
                                        new ConditionNode(monster -> {
                                            boolean empty = MonsterService.isZero(monster.getWalkingTo());
                                            if (debug) System.out.println("[Condition] " + monster.getName() + " walkingTo isZero? " + empty);
                                            return empty;
                                        }),
                                        new ActionNode(monster -> { // TODO: add check for "stuck" and reset walkingTo().
                                            // if the monster does not move more than 1f in 10 seconds, set Stuck status.
                                            final float MAX_SLOPE_DEG = 45f;
                                            final int   MAX_TRIES     = 16;
                                            final float PROBE_UP      = 3f;
                                            final float PROBE_DOWN    = 10f;

                                            Vector3f origin = monster.getPosition();
                                            Vector3f pos = Utils.pickRandomWalkableAround(
                                                    origin,
                                                    monster.getMaxIdleWalkDst(),
                                                    MAX_SLOPE_DEG,
                                                    MAX_TRIES,
                                                    PROBE_UP, PROBE_DOWN,
                                                    monster // para filtrar self
                                            );

                                            if (pos != null) {
                                                if (debug) System.out.println("[Action] " + monster.getName() + " idle walkingTo: " + pos);
                                                monster.setWalkingTo(pos);
                                            } else {
                                                if (debug) System.out.println("[Action] " + monster.getName() + " idle: nenhuma posição walkable encontrada");
                                            }
                                        })
                                )),


                                // 1c. walkingTo != null → continue andando
                                new SequenceNode(List.of(
                                        new ConditionNode(monster -> {
                                            Vector3f goal = monster.getWalkingTo();
                                            if (MonsterService.isZero(goal)) {
                                                if (debug) System.out.println("[Condition] " + monster.getName() + " walkingTo empty → false");
                                                return false;
                                            }
                                            // alinhar Y suave (sem zerar alvo por microdiferença)
                                            if (!MonsterService.epsilonEquals(goal.y, monster.getPosition().y, 1e-3f)) {
                                                goal = goal.clone();
                                                goal.y = monster.getPosition().y;
                                                monster.setWalkingTo(goal);
                                            }
                                            // já chegou? (usa sua função)
                                            boolean reached = MonsterService.reachedWalkingTarget(monster);
                                            if (reached) {
                                                if (debug) System.out.println("[Condition] " + monster.getName() + " reached target → false (vai resetar por evento/loop)");
                                                // deixa false aqui; quem zera é o fluxo no update/handler
                                                return false;
                                            }
                                            if (debug) System.out.println("[Condition] " + monster.getName() + " walkingTo set & not reached → true");
                                            return true;
                                        }),
                                        new ActionNode(monster -> {
                                            if (debug) System.out.println("[Action] " + monster.getName() + " walking to " + monster.getWalkingTo());
                                            bus.post(new MonsterWalkEvent(monster.getId(), monster.getWalkingTo()));
                                        })
                                ))

                        ))
                )),

                // 2️⃣ Se aggro não é null → andar até a posição do aggro
                new SequenceNode(List.of(
                        new ConditionNode(monster -> {
                            if(monster.getAggro() == null) return false;
                            boolean shouldPerformAction = monster.getPosition().subtract(
                                    monster.getAggro().getPosition()
                            ).length() <= monster.getStats().getMaxSeeDistance();
                            if (!shouldPerformAction) {
                                monster.setAggro(null);
                                monster.setWalkingTo(Vector3f.ZERO);
                            }
                            if (debug) System.out.println("[Condition] " + monster.getName() + " has aggro? " + shouldPerformAction);
                            return shouldPerformAction;
                        }),
                        new ActionNode(monster -> {
                            Vector3f target = monster.getAggro().getPosition().clone();
                            target.y = monster.getPosition().y; // planariza rumo ao alvo
                            if (debug) System.out.println("[Action] " + monster.getName() + " chase to " + target);
                            monster.setWalkingTo(target);
                            bus.post(new MonsterWalkEvent(monster.getId(), target));
                        })
                ))
//                ,
//                // 3️⃣ Fallback / Idle
//                new ActionNode(monster -> {
//
//                    bus.post(new MonsterIdleEvent(monster));
//                })
        ));

        return new BehaviourTree(root);
    }
}
