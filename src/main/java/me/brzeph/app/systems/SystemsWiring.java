package me.brzeph.app.systems;

import me.brzeph.app.systems.impl.*;
import me.brzeph.core.domain.gui.core.screens.UIScreenClickEvent;
import me.brzeph.infra.events.chat.ChatScroll;
import me.brzeph.infra.events.chat.ChatToggle;
import me.brzeph.infra.events.entities.enemies.MonsterAggroEvent;
import me.brzeph.infra.events.entities.enemies.MonsterSpawnEvent;
import me.brzeph.infra.events.entities.enemies.MonsterWalkEvent;
import me.brzeph.infra.events.entities.player.PlayerJumpEvent;
import me.brzeph.infra.events.entities.player.PlayerRunEvent;
import me.brzeph.infra.events.entities.player.PlayerWalkEvent;
import me.brzeph.infra.events.items.DropItemEvent;

public final class SystemsWiring {

    public static void wireSystems() {
         ItemSystem       itemSystem      = (ItemSystem      ) SystemAbs.getSystem(ItemSystem      .class);
//         DefaultGUISystem guiSystem       = (DefaultGUISystem) SystemAbs.getSystem(DefaultGUISystem.class); Wiring desta classe é interno.
         MonsterSystem    monsterSystem   = (MonsterSystem   ) SystemAbs.getSystem(MonsterSystem   .class);
         ChatSystem       chatSystem      = (ChatSystem      ) SystemAbs.getSystem(ChatSystem      .class);
         CameraSystem     cameraSystem    = (CameraSystem    ) SystemAbs.getSystem(CameraSystem    .class);
         PlayerSystem     playerSystem    = (PlayerSystem    ) SystemAbs.getSystem(PlayerSystem    .class);

        itemSystem.getBus().subscribe(DropItemEvent.class, itemSystem::DropItemEvent);

        monsterSystem.getBus().subscribe(MonsterWalkEvent.class,  monsterSystem::onWalkEvent);
        monsterSystem.getBus().subscribe(MonsterSpawnEvent.class, monsterSystem::onSpawnEvent);
        monsterSystem.getBus().subscribe(MonsterAggroEvent.class, monsterSystem::onAggroEvent);

        chatSystem.getBus().subscribe(ChatToggle.class, chatSystem::chatToggle);
        chatSystem.getBus().subscribe(ChatScroll.class, chatSystem::chatScroll);

        playerSystem.getBus().subscribe(PlayerWalkEvent.class, playerSystem::onWalkAction);
        playerSystem.getBus().subscribe(PlayerJumpEvent.class, playerSystem::onJumpAction);
        playerSystem.getBus().subscribe(PlayerRunEvent.class , playerSystem::onTriggerRunAction);
    }
}
