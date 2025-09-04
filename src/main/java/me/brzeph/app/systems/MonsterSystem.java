package me.brzeph.app.systems;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.behaviour.GameStateContext;
import me.brzeph.core.domain.entity.enemies.melee.impl.Goblin;
import me.brzeph.core.factory.MonsterFactory;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.entities.enemies.MonsterAggroEvent;
import me.brzeph.infra.events.entities.enemies.MonsterSpawnEvent;
import me.brzeph.infra.events.entities.enemies.MonsterWalkEvent;
import me.brzeph.infra.jme.adapter.audio.MonsterAudioAdapter;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.ArrayList;
import java.util.List;

public class MonsterSystem {
    private final Node root;
    private final EventBus bus;
    private final CharacterPhysicsAdapter physicsAdapter;
    private final MonsterAudioAdapter audioAdapter;
    private final MonsterFactory monsterFactory;
    private final GameStateContext gameStateContext;

    private final List<Monster> monsterList = new ArrayList<>();

    public MonsterSystem(
            Node root,
            EventBus bus,
            CharacterPhysicsAdapter physicsAdapter,
            MonsterAudioAdapter audioAdapter,
            MonsterFactory monsterFactory
    ) {
        this.root = root;
        this.bus = bus;
        this.physicsAdapter = physicsAdapter;
        this.audioAdapter = audioAdapter;
        this.monsterFactory = monsterFactory;
        this.gameStateContext = GameStateContext.getContext();
        initialize();
    }

    public static void initMonster(EventBus bus) { // Eventualmente será substituído por initSpawners().
        Player pl = GameStateContext.getContext().getList(Player.class).getFirst();
        for (int i = 0; i < 5; i ++) {
            bus.post(
                    new MonsterSpawnEvent(
                            new Goblin(
                                    pl.getPosition().add(new Vector3f(0, 3 + i, 0)),
                                    pl.getRotation()
                            )
                    )
            );
        }
    }

    private void initialize(){
        bus.subscribe(MonsterWalkEvent.class,  this::onWalkEvent);
        bus.subscribe(MonsterSpawnEvent.class, this::onSpawnEvent);
        bus.subscribe(MonsterAggroEvent.class, this::onAggroEvent);
    }

    public void update(float tpf) {
        for (Monster monster : monsterList) {
            Vector3f vel = monster.update(root, tpf); // pode ser null
            if (vel != null && vel.lengthSquared() > 0f) {
                physicsAdapter.moveCharacter(monster, vel); // setWalkDirection(m/s)
            } else {
                physicsAdapter.moveCharacter(monster, Vector3f.ZERO);
            }
        }
    }

    private void onAggroEvent(MonsterAggroEvent monsterAggroEvent) {
        Monster monster = monsterAggroEvent.monster();
        monster.setWalkingTo(Vector3f.ZERO);
        monster.setAggro(monsterAggroEvent.player());
    }

    private void onSpawnEvent(MonsterSpawnEvent monsterSpawnEvent) {
        Monster monster = monsterSpawnEvent.monster();
        gameStateContext.get(ChatSystem.class).send(ChatChannel.GLOBAL, "", "Spawning monster: " + monster.getId());
        monsterFactory.setupCharacter(monster, root);
        monsterList.add(monster);
        audioAdapter.playSoundAt(monster, "spawn_sound");
    }

    private void onWalkEvent(MonsterWalkEvent monsterWalkEvent) {
        Monster monster = (Monster) GameEntityRepository.findById(monsterWalkEvent.monsterId());
        if (monster == null) return;
        monster.setWalkingTo(monsterWalkEvent.walkingTo());
    }
}
