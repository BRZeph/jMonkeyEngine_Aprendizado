package me.brzeph.domain.entity.enemies;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.domain.entity.CharacterEntity;
import me.brzeph.domain.entity.CharacterStats;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.player.Player;
import me.brzeph.domain.entity.enemies.behaviour.BehaviourTree;
import me.brzeph.app.service.MonsterService;
import me.brzeph.constants.EnemiesConstants;

import java.util.List;

import static me.brzeph.domain.entity.enemies.behaviour.Utils.canSee;

public abstract class Monster extends CharacterEntity {

    private final MonsterBehaviour monsterBehaviour;
    private Vector3f walkingTo = Vector3f.ZERO;
    private CharacterEntity aggro;
    private final float maxIdleWalkDst;

    public Monster(
            EntityType type, Vector3f position, Quaternion rotation, String name,
            CharacterStats stats,
            MonsterBehaviour monsterBehaviour
    ) {
        super(type, position, rotation, name, stats);
        this.monsterBehaviour = monsterBehaviour;
        this.aggro = null;
        maxIdleWalkDst = 10f;
    }

    public Vector3f update(Node root, float tpf) {
        getBehaviourTree().tick(this);

        Spatial s = root.getChild(this.getId());
        if (s != null) this.setPosition(s.getWorldTranslation().clone());

        Vector3f target = this.getWalkingTo();
        if (!MonsterService.isZero(target)) {
            if (MonsterService.reachedWalkingTarget(this)) {
                this.setWalkingTo(Vector3f.ZERO);
                return null;
            } else {
                Vector3f v = MonsterService.getWalkingVelocity(this, tpf);
                if (MonsterService.willOvershoot(this, tpf)) {
                    this.setWalkingTo(Vector3f.ZERO); // próximo frame já estará em cima do alvo
                }
                return v; // m/s
            }
        } else {
            return null;
        }
    }

    public boolean canAttackTarget() { // Método pequeno e direito, vou manter aqui ao invés do MonsterService.class
        return this.canSeeTarget() && stats.getBasicAttackRange() <= position.distance(aggro.getPosition());
    }

    public boolean canSeeTarget() { // Método pequeno e direito, vou manter aqui ao invés do MonsterService.class
        return canSee(this, aggro, stats.getMaxSeeDistance(), monsterBehaviour.getFov());
    }

    public CharacterEntity canSeeTarget(List<Player> list){
        for (Player p : list) {
            if(canSee(this, p, stats.getMaxSeeDistance(), monsterBehaviour.getFov())) {
                return p;
            }
        }
        return null;
    }

    private BehaviourTree getBehaviourTree(){
        return EnemiesConstants.BEHAVIOUR_TREE_MAP.get(monsterBehaviour);
    }

    public Vector3f getWalkingTo() {
        return walkingTo;
    }

    public void setWalkingTo(Vector3f walkingTo) {
        this.walkingTo = walkingTo;
    }

    public MonsterBehaviour getMonsterType() {
        return monsterBehaviour;
    }

    public CharacterEntity getAggro() {
        return aggro;
    }

    public void setAggro(CharacterEntity aggro) {
        this.aggro = aggro;
    }

    public float getMaxIdleWalkDst() {
        return maxIdleWalkDst;
    }
}
