package me.brzeph.app.systems.impl;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.domain.entity.item.DroppedItem;
import me.brzeph.domain.entity.item.ItemStack;
import me.brzeph.app.factory.ItemFactory;
import me.brzeph.events.items.DropItemEvent;
import me.brzeph.infra.repository.GameEntityRepository;

import java.util.ArrayList;
import java.util.List;

import static me.brzeph.constants.ItemConstants.COIN_DEF;


public class ItemSystem extends SystemAbs {
    private final List<String> droppedItems = new ArrayList<>();

    private static final float OSCILLATION_SPEED = 2f; // Velocidade do movimento (quanto maior, mais rápido o item sobe e desce)
    private float baseHeight = 3f; // A altura base do item, a partir do chão
    private float amplitude = 1f; // Distância de subida e descida
    private float currentTime = 0f; // Variável para controlar o tempo

    public ItemSystem() {

    }

    public void initialize(){
        DroppedItem drop = ItemFactory.createDropFrom(
                new ItemStack(COIN_DEF, 25),
                new Vector3f(10, 0, 5),
                Quaternion.IDENTITY
        );
        getBus().post(new DropItemEvent(drop));
    }

    public void update(float tpf){
        for (String s : droppedItems){
            DroppedItem item = (DroppedItem) GameEntityRepository.findById(s);
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
        if(item == null) return;
        getEntityFactory().build(item);
        droppedItems.add(item.getId());
    }

    public void deSpawn(DroppedItem e) {
        if (!droppedItems.contains(e.getId())) {
            throw new IllegalArgumentException("Attempted to despawn item that wasn't dropped");
        }
        droppedItems.remove(e.getId());
        GameEntityRepository.remove(e.getId());
    }
}
