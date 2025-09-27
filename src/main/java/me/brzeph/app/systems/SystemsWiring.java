package me.brzeph.app.systems;

import me.brzeph.app.systems.impl.*;
import me.brzeph.app.systems.impl.collisionSystem.handlers.ItemProximitySystem;
import me.brzeph.app.systems.impl.collisionSystem.events.DropConsumedEvent;
import me.brzeph.app.systems.impl.collisionSystem.events.ItemProximityEnter;
import me.brzeph.app.systems.impl.collisionSystem.events.ItemProximityExit;
import me.brzeph.events.entities.enemies.MonsterAggroEvent;
import me.brzeph.events.entities.enemies.MonsterSpawnEvent;
import me.brzeph.events.entities.enemies.MonsterWalkEvent;
import me.brzeph.events.entities.player.PlayerJumpEvent;
import me.brzeph.events.entities.player.PlayerRunEvent;
import me.brzeph.events.entities.player.PlayerWalkEvent;
import me.brzeph.events.items.DropItemEvent;

public final class SystemsWiring {

    public static void wireSystems() {
        ItemSystem          itemSystem          = SystemAbs.getSystem(ItemSystem         .class);
        MonsterSystem       monsterSystem       = SystemAbs.getSystem(MonsterSystem      .class);
        CameraSystem        cameraSystem        = SystemAbs.getSystem(CameraSystem       .class);
        PlayerSystem        playerSystem        = SystemAbs.getSystem(PlayerSystem       .class);
        ItemProximitySystem itemProximitySystem = SystemAbs.getSystem(ItemProximitySystem.class);

        itemSystem.getBus().subscribe(DropItemEvent.class, itemSystem::DropItemEvent);

        monsterSystem.getBus().subscribe(MonsterWalkEvent.class,  monsterSystem::onWalkEvent);
        monsterSystem.getBus().subscribe(MonsterSpawnEvent.class, monsterSystem::onSpawnEvent);
        monsterSystem.getBus().subscribe(MonsterAggroEvent.class, monsterSystem::onAggroEvent);

        playerSystem.getBus().subscribe(PlayerWalkEvent.class, playerSystem::onWalkAction);
        playerSystem.getBus().subscribe(PlayerJumpEvent.class, playerSystem::onJumpAction);
        playerSystem.getBus().subscribe(PlayerRunEvent.class , playerSystem::onTriggerRunAction);

        itemProximitySystem.getBus().subscribe(ItemProximityEnter.class, itemProximitySystem::itemProximityEnterEventHandler);
        itemProximitySystem.getBus().subscribe(ItemProximityExit.class, itemProximitySystem::itemProximityExitEventHandler);
        itemProximitySystem.getBus().subscribe(DropConsumedEvent.class, itemProximitySystem::itemFullyConsumedHandler);
    }
}
