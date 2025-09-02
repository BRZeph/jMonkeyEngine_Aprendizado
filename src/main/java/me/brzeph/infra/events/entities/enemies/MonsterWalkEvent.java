package me.brzeph.infra.events.entities.enemies;

import com.jme3.math.Vector3f;
import me.brzeph.infra.jme.adapter.utils.InputAction;

import java.util.Vector;

public record MonsterWalkEvent(String monsterId, Vector3f walkingTo){

}
