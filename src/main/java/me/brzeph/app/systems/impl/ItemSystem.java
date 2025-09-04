package me.brzeph.app.systems.impl;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.app.systems.System;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.item.DroppedItem;
import me.brzeph.core.factory.EntityFactory;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.items.DropItemEvent;
import me.brzeph.infra.jme.adapter.physics.EntityPhysicsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ItemSystem extends System {
    private final List<DroppedItem> droppedItems = new ArrayList<>();
    private final ChatSystem chatSystem;

    private static final float OSCILLATION_SPEED = 2f; // Velocidade do movimento (quanto maior, mais rápido o item sobe e desce)
    private float baseHeight = 3f; // A altura base do item, a partir do chão
    private float amplitude = 1f; // Distância de subida e descida
    private float currentTime = 0f; // Variável para controlar o tempo

    public ItemSystem() {
        chatSystem = (ChatSystem) getSystem(ChatSystem.class);
        initialize();
    }

    public void initialize(){
        getBus().post(new DropItemEvent(
                new DroppedItem(
                        new Vector3f(5,3,5),
                        new Quaternion(0,0,0,1),
                        "Item",
                        null,
                        0.2f,
                        0,
                        32
                )
        ));
    }

    public void subscribe(){
        getBus().subscribe(DropItemEvent.class, this::DropItemEvent);
    }

    public void update(float tpf){
        for (DroppedItem item : droppedItems){
            chatSystem.send(ChatChannel.GLOBAL, "", "" + item.getPosition());

            // Atualiza o tempo para gerar um movimento cíclico (como uma função seno)
            currentTime += tpf * OSCILLATION_SPEED; // Ajuste a velocidade da oscilação

            // Calcula o novo valor de Y com um movimento cíclico (usando a função seno)
            float yOffset = amplitude * FastMath.sin(currentTime);

            Spatial spatial = getEntityPhysicsAdapter().getControl(item.getId()).getSpatial();

            // Define a nova posição do item, mantendo a posição X e Z inalteradas, mas alterando a Y
            Vector3f currentPosition = spatial.getLocalTranslation();
            currentPosition.y = baseHeight + yOffset; // Distância ao chão + movimento cíclico
            spatial.setLocalTranslation(currentPosition);
            item.setPosition(currentPosition);
        }
    }

    private void DropItemEvent(DropItemEvent dropItemEvent) {
        DroppedItem item = dropItemEvent.item();
        chatSystem.send(ChatChannel.GLOBAL, "", "Spawning item: " + item);
        if(item == null) return;
        droppedItems.add(dropItemEvent.item());
        getEntityFactory().setupCharacter(item, getRoot());
        getEntityPhysicsAdapter().getControl(item.getId()).setGravity(Vector3f.ZERO);
    }
}
