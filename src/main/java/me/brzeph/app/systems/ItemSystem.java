package me.brzeph.app.systems;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.enemies.behaviour.GameStateContext;
import me.brzeph.core.domain.item.DroppedItem;
import me.brzeph.core.factory.MonsterFactory;
import me.brzeph.infra.events.EventBus;
import me.brzeph.infra.events.items.DropItemEvent;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ItemSystem {
    private final Node root;
    private final EventBus bus;
    private final CharacterPhysicsAdapter physicsAdapter;
    private final MonsterFactory factory;

    private final GameStateContext gameStateContext;
    private final List<DroppedItem> droppedItems;

    private static final float OSCILLATION_SPEED = 2f; // Velocidade do movimento (quanto maior, mais rápido o item sobe e desce)
    private float baseHeight; // A altura base do item, a partir do chão
    private float amplitude; // Distância de subida e descida
    private float currentTime = 0f; // Variável para controlar o tempo

    public ItemSystem(
            Node root,
            EventBus bus,
            CharacterPhysicsAdapter physicsAdapter,
            MonsterFactory factory // Usando a do monster por enquanto, depois refatorar com 1 único EntityFactory
    ) {
        this.root = root;
        this.bus = bus;
        this.physicsAdapter = physicsAdapter;
        this.factory = factory;
        this.droppedItems = new ArrayList<>();
        this.gameStateContext = GameStateContext.getContext();

        baseHeight = 3f;
        amplitude = 1;

        initialize();
    }

    public void initialize(){
        subscribe();
        bus.post(new DropItemEvent(
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
        bus.subscribe(DropItemEvent.class, this::DropItemEvent);
    }

    public void update(float tpf){
        for (DroppedItem item : droppedItems){
            gameStateContext.get(ChatSystem.class).send(ChatChannel.GLOBAL, "", "" + item.getPosition());

            // Atualiza o tempo para gerar um movimento cíclico (como uma função seno)
            currentTime += tpf * OSCILLATION_SPEED; // Ajuste a velocidade da oscilação

            // Calcula o novo valor de Y com um movimento cíclico (usando a função seno)
            float yOffset = amplitude * FastMath.sin(currentTime);

            Spatial spatial = physicsAdapter.getControl(item.getId()).getSpatial();

            // Define a nova posição do item, mantendo a posição X e Z inalteradas, mas alterando a Y
            Vector3f currentPosition = spatial.getLocalTranslation();
            currentPosition.y = baseHeight + yOffset; // Distância ao chão + movimento cíclico
            spatial.setLocalTranslation(currentPosition);
            item.setPosition(currentPosition);
        }
    }

    private void DropItemEvent(DropItemEvent dropItemEvent) {
        DroppedItem item = dropItemEvent.item();
        gameStateContext.get(ChatSystem.class).send(ChatChannel.GLOBAL, "", "Spawning item: " + item);
        if(item == null) return;
        droppedItems.add(dropItemEvent.item());
        factory.setupCharacter(item, root);
        physicsAdapter.getControl(item.getId()).setGravity(Vector3f.ZERO);
    }
}
