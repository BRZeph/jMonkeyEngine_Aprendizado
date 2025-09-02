package me.brzeph.core.factory;

import com.jme3.asset.AssetManager;
import me.brzeph.infra.jme.adapter.physics.CharacterPhysicsAdapter;

public class PlayerFactory extends CharacterFactory{
    public PlayerFactory(AssetManager assetManager, CharacterPhysicsAdapter physicsAdapter) {
        super(assetManager, physicsAdapter);
    }
}
