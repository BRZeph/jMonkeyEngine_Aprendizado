package me.brzeph.app.systems.impl;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.entity.item.DroppedItem;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.factory.ItemFactory;
import me.brzeph.infra.events.items.DropItemEvent;

import java.util.ArrayList;
import java.util.List;

import static me.brzeph.core.constants.ItemConstants.COIN_DEF;
import static me.brzeph.core.constants.ItemConstants.PREFAB_RESOLVER;


public class ItemSystem extends SystemAbs {
    private final List<DroppedItem> droppedItems = new ArrayList<>();
    private final ChatSystem chatSystem;

    private static final float OSCILLATION_SPEED = 2f; // Velocidade do movimento (quanto maior, mais rápido o item sobe e desce)
    private float baseHeight = 3f; // A altura base do item, a partir do chão
    private float amplitude = 1f; // Distância de subida e descida
    private float currentTime = 0f; // Variável para controlar o tempo

    public ItemSystem() {
        chatSystem = (ChatSystem) getSystem(ChatSystem.class);
    }

    public void initialize(){
        DroppedItem drop = ItemFactory.createDropFrom(
                new ItemInstance(COIN_DEF, 25),
                new Vector3f(10, 0, 5),
                Quaternion.IDENTITY,
                PREFAB_RESOLVER
        );
        getBus().post(new DropItemEvent(drop));
    }

    public void update(float tpf){
        for (DroppedItem item : droppedItems){
            currentTime += tpf * OSCILLATION_SPEED; // Ajuste a velocidade da oscilação

            float yOffset = amplitude * FastMath.sin(currentTime);
            Node spatial = item.getCharacterNode();

            // Define a nova posição do item, mantendo a posição X e Z inalteradas, mas alterando a Y
            Vector3f currentPosition = spatial.getLocalTranslation();
            currentPosition.y = baseHeight + yOffset; // Distância ao chão + movimento cíclico
            spatial.setLocalTranslation(currentPosition);
            item.setPosition(currentPosition);
        }
    }

    public void DropItemEvent(DropItemEvent dropItemEvent) {
        DroppedItem item = dropItemEvent.item();
        chatSystem.send(ChatChannel.GLOBAL, "", "Spawning item: " + item);
        if(item == null) return;
        getEntityFactory().build(item, getRoot());
        droppedItems.add(item);
    }
}
