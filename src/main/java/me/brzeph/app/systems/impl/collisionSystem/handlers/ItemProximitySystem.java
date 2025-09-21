package me.brzeph.app.systems.impl.collisionSystem.handlers;

import me.brzeph.app.systems.SystemAbs;
import me.brzeph.app.systems.impl.collisionSystem.CollisionSystem;
import me.brzeph.app.systems.impl.collisionSystem.events.DropConsumedEvent;
import me.brzeph.app.systems.impl.collisionSystem.events.ItemProximityEnter;
import me.brzeph.app.systems.impl.collisionSystem.events.ItemProximityExit;
import me.brzeph.core.domain.entity.GameEntity;
import me.brzeph.core.domain.entity.enemies.Monster;
import me.brzeph.core.domain.entity.item.DroppedItem;
import me.brzeph.core.domain.entity.player.Player;
import me.brzeph.app.systems.impl.InventorySystem;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemProximitySystem extends SystemAbs {

    public final HashMap<String, List<String>> colliding = new HashMap<>();

    @Override
    public void update(float tpf) {
        handlePlayerItemStay();
        handleMonsterItemStay();
    }

    public void itemFullyConsumedHandler(DropConsumedEvent e) {
        DroppedItem ea = (DroppedItem) GameEntityRepository.findById(e.dropId());
        ea.deSpawn((CollisionSystem) getSystem(CollisionSystem.class));
    }

    public void itemProximityEnterEventHandler(ItemProximityEnter e){
        GameEntity ea = e.entity();
        GameEntity eb = e.item();

        if (! (eb instanceof DroppedItem i)){
            throw new IllegalArgumentException("ItemProximityEnterEventHandler requires DroppedItem");
        }

        if (ea instanceof Player p){
            handlePlayerItemEnter(p, i);
        } else if (ea instanceof Monster m){
            handleMonsterItemEnter(m,i); // Atualmente não tenho este cenário mapeado, mas estou deixando aqui pronto.
        }
    }

    public void itemProximityExitEventHandler(ItemProximityExit e){
        GameEntity ea = e.entity();
        GameEntity eb = e.item();

        if (! (eb instanceof DroppedItem i)){
            throw new IllegalArgumentException("ItemProximityEnterEventHandler requires DroppedItem");
        }

        if (ea instanceof Player p){
            handlePlayerItemExit(p, i);
        } else if (ea instanceof Monster m){
            handleMonsterItemExit(m,i); // Atualmente não tenho este cenário mapeado, mas estou deixando aqui pronto.
        }
    }

    private void handlePlayerItemEnter(Player ea, DroppedItem eb) {
        if(ItemProximityService.attemptPlayerCollectItem((InventorySystem) getSystem(InventorySystem.class), ea, eb)){
//            eb.deSpawn((CollisionSystem) getSystem(CollisionSystem.class)); // DeSpawn feito por DropConsumedEvent
        } else {
            List<String> eaColliding = colliding.containsKey(ea.getId()) ? colliding.get(ea.getId()) : new ArrayList<>();
            eaColliding.add(ea.getId());
            colliding.put(ea.getId(), eaColliding);
        }
    }

    private void handlePlayerItemStay() {
        List<String> toRemoveEa = new ArrayList<>();
        for (String keySet : colliding.keySet()) {
            List<String> toRemoveEb = new ArrayList<>();
            List<String> sl = colliding.get(keySet);
            Player ea = (Player) GameEntityRepository.findById(keySet);

            for (String s : sl){

                DroppedItem eb = (DroppedItem) GameEntityRepository.findById(s);
                if(ItemProximityService.attemptPlayerCollectItem((InventorySystem) getSystem(InventorySystem.class), ea, eb)){
                    eb.deSpawn((CollisionSystem) getSystem(CollisionSystem.class));
                    toRemoveEb.add(s);
                }
            }

            sl.removeAll(toRemoveEb);
            if (sl.isEmpty()){
                toRemoveEa.add(keySet);
            }
        }
        for (String s : toRemoveEa){
            colliding.remove(s);
        }
    }

    private void handlePlayerItemExit(Player p, DroppedItem i) {
        colliding.remove(p.getId());
    }

    private void handleMonsterItemEnter(Monster ea, DroppedItem eb) {
        // Atualmente não tenho este cenário mapeado, mas estou deixando aqui pronto.
        throw new RuntimeException("Collision between monster");
    }

    private void handleMonsterItemStay() {

    }

    private void handleMonsterItemExit(Monster m, DroppedItem i) {
        // Atualmente não tenho este cenário mapeado, mas estou deixando aqui pronto.
        throw new RuntimeException("Collision between monster");
    }

}
