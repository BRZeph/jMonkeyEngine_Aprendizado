package me.brzeph.app;

import me.brzeph.app.systems.CombatSystem;
import me.brzeph.app.systems.QuestSystem;
import me.brzeph.app.systems.TimeSystem;
import me.brzeph.app.systems.SaveSystem;
import me.brzeph.infra.events.EventBus;

public final class SystemsWiring {

    private SystemsWiring() {}

    public static void register(EventBus bus,
                                CombatSystem combat,
                                QuestSystem quest,
                                TimeSystem time,
                                SaveSystem save) {

//        // exemplo: quando alguém pedir ataque, delega ao CombatSystem
//        bus.subscribe("AttackRequestedEvent", evt -> combat.onAttack(evt));
//
//        // quando a quest for atualizada
//        bus.subscribe("QuestAcceptedEvent", evt -> quest.onQuestAccepted(evt));
//        bus.subscribe("QuestCompletedEvent", evt -> quest.onQuestCompleted(evt));
//
//        // tempo correndo (pode vir de um TickEvent)
//        bus.subscribe("TickEvent", evt -> time.onTick(evt));
//
//        // salvar estado
//        bus.subscribe("SaveRequestedEvent", evt -> save.onSave(evt));
    }
}
