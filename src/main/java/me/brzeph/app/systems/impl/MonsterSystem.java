package me.brzeph.app.systems.impl;

import com.jme3.math.Vector3f;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.EntityType;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.enemies.melee.impl.Goblin;
import me.brzeph.infra.events.entities.enemies.MonsterAggroEvent;
import me.brzeph.infra.events.entities.enemies.MonsterSpawnEvent;
import me.brzeph.infra.events.entities.enemies.MonsterWalkEvent;
import me.brzeph.infra.jme.adapter.audio.MonsterAudioAdapter;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.ArrayList;
import java.util.List;

import static me.brzeph.core.constants.EnemiesConstants.GOBLIN_BASE_SPEED;

public class MonsterSystem extends SystemAbs {

    private final List<Monster> monsterList = new ArrayList<>();
    private final PlayerSystem playerSystem;
    private final MonsterAudioAdapter monsterAudio;

    public MonsterSystem() {
        playerSystem = (PlayerSystem) getSystem(PlayerSystem.class);
        monsterAudio = new MonsterAudioAdapter(getAssetManager());
    }

    public void initMonster() { // Eventualmente será substituído por initSpawners().
        Player pl = playerSystem.getPlayer();
        for (int i = 0; i < 5; i ++) {
            getBus().post(
                    new MonsterSpawnEvent(
                            new Goblin(
                                    EntityType.ORC_BRUTE,
                                    pl.getPosition().add(new Vector3f(0, 3 + i, 0)),
                                    pl.getRotation(),
                                    "Goblin",
                                    new CharacterStats(
                                            1, 1, 1, GOBLIN_BASE_SPEED, 30f, 2f
                                    )
                            )
                    )
            );
        }
    }

    public void update(float tpf) {
        if (monsterList.isEmpty()) {
            initMonster();
        }
        for (Monster monster : monsterList) {
            Vector3f vel = monster.update(getRoot(), tpf); // pode ser null
            if (vel != null && vel.lengthSquared() > 0f) {
                getEntityPhysicsAdapter().moveCharacter(monster, vel); // setWalkDirection(m/s)
            } else {
                getEntityPhysicsAdapter().moveCharacter(monster, Vector3f.ZERO);
            }
        }
    }

    public void onAggroEvent(MonsterAggroEvent monsterAggroEvent) {
        Monster monster = monsterAggroEvent.monster();
        monster.setWalkingTo(Vector3f.ZERO);
        monster.setAggro(monsterAggroEvent.player());
    }

    public void onSpawnEvent(MonsterSpawnEvent monsterSpawnEvent) {
        Monster monster = monsterSpawnEvent.monster();
        monsterList.add(monster);
        getEntityFactory().build(monster, getRoot());
        monsterAudio.playSoundAt(monster, "spawn_sound");
    }

    public void onWalkEvent(MonsterWalkEvent monsterWalkEvent) {
        Monster monster = (Monster) GameEntityRepository.findById(monsterWalkEvent.monsterId());
        if (monster == null) return;
        monster.setWalkingTo(monsterWalkEvent.walkingTo());
    }
}
