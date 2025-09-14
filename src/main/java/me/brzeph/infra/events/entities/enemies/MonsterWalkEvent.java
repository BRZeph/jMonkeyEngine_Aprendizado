package me.brzeph.infra.events.entities.enemies;

import com.jme3.math.Vector3f;

import java.util.Vector;

public record MonsterWalkEvent(String monsterId, Vector3f walkingTo){

}
