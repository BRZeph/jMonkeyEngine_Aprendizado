package me.brzeph.app.systems.impl;

import com.jme3.math.Vector3f;
import me.brzeph.app.systems.System;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.melee.impl.Goblin;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.entities.enemies.MonsterAggroEvent;
import me.brzeph.infra.events.entities.enemies.MonsterSpawnEvent;
import me.brzeph.infra.events.entities.enemies.MonsterWalkEvent;
import me.brzeph.infra.jme.adapter.audio.MonsterAudioAdapter;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.ArrayList;
import java.util.List;

public class MonsterSystem extends System {

    private final List<Monster> monsterList = new ArrayList<>();
    private final PlayerSystem playerSystem;
    private final ChatSystem chatSystem;
    private final MonsterAudioAdapter monsterAudio;

    public MonsterSystem() {
        playerSystem = (PlayerSystem) getSystem(PlayerSystem.class);
        chatSystem = (ChatSystem) getSystem(ChatSystem.class);
        monsterAudio = new MonsterAudioAdapter(getAssetManager());
        initMonster();
    }

    public void initMonster() { // Eventualmente será substituído por initSpawners().
        Player pl = playerSystem.getPlayer();
        for (int i = 0; i < 5; i ++) {
            getBus().post(
                    new MonsterSpawnEvent(
                            new Goblin(
                                    pl.getPosition().add(new Vector3f(0, 3 + i, 0)),
                                    pl.getRotation()
                            )
                    )
            );
        }
    }

    @Override
    public void subscribe() {
        getBus().subscribe(MonsterWalkEvent.class,  this::onWalkEvent);
        getBus().subscribe(MonsterSpawnEvent.class, this::onSpawnEvent);
        getBus().subscribe(MonsterAggroEvent.class, this::onAggroEvent);
    }

    public void update(float tpf) {
        for (Monster monster : monsterList) {
            Vector3f vel = monster.update(getRoot(), tpf); // pode ser null
            if (vel != null && vel.lengthSquared() > 0f) {
                getEntityPhysicsAdapter().moveCharacter(monster, vel); // setWalkDirection(m/s)
            } else {
                getEntityPhysicsAdapter().moveCharacter(monster, Vector3f.ZERO);
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
        chatSystem.send(ChatChannel.GLOBAL, "", "Spawning monster: " + monster.getId());
        getEntityFactory().setupCharacter(monster, getRoot());
        monsterList.add(monster);
        monsterAudio.playSoundAt(monster, "spawn_sound");
    }

    private void onWalkEvent(MonsterWalkEvent monsterWalkEvent) {
        Monster monster = (Monster) GameEntityRepository.findById(monsterWalkEvent.monsterId());
        if (monster == null) return;
        monster.setWalkingTo(monsterWalkEvent.walkingTo());
    }
}
