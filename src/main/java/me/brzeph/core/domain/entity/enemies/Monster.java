package me.brzeph.core.domain.entity.enemies;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.entity.Character;
import me.brzeph.core.domain.entity.CharacterStats;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.behaviour.BehaviourTree;
import me.brzeph.core.service.MonsterService;
import me.brzeph.infra.constants.EnemiesConstants;

import java.util.List;

import static me.brzeph.core.domain.entity.enemies.behaviour.Utils.canSee;

public abstract class Monster extends Character {

    private final MonsterBehaviour monsterBehaviour;
    private Vector3f walkingTo = Vector3f.ZERO;
    private Character aggro;
    private final float maxIdleWalkDst;

    public Monster(
            Vector3f position, Quaternion rotation, String name,
            CharacterStats stats,
            float height, float weight, float jumpForce,
            MonsterBehaviour monsterBehaviour
    ) {
        super(position, rotation, name, stats, height, weight, jumpForce);
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
                // se este frame alcança o alvo, já podemos zerar o walkingTo (evita loop extra)
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

    public Character canSeeTarget(List<Player> list){
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

    public Character getAggro() {
        return aggro;
    }

    public void setAggro(Character aggro) {
        this.aggro = aggro;
    }

    public float getMaxIdleWalkDst() {
        return maxIdleWalkDst;
    }
}
