package me.brzeph.core.factory;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;

public class MonsterFactory extends CharacterFactory{
    public MonsterFactory(AssetManager assetManager, CharacterPhysicsAdapter physicsAdapter) {
        super(assetManager, physicsAdapter);
    }

    @Override
    public Node setupCharacter(Character character, Node root) {
        /*
            Dar OverRide aqui caso preciso.
            Se precisar, criar um MageMonsterFactory ou algo do tipo para monstros específicos.
         */
        return super.setupCharacter(character, root);
    }
}
