package me.brzeph.infra.jme.adapter.renderer;

import com.jme3.anim.AnimComposer;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;

public class MonsterRenderAdapter {

    private final Node rootNode;

    public MonsterRenderAdapter(Node rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Atualiza posição/rotação do modelo 3D do monster.
     */
    public void updatePosition(Monster monster) {
        Spatial model = rootNode.getChild(monster.getId());
        if (model != null) {
            model.setLocalTranslation(monster.getPosition());
            model.setLocalRotation(monster.getRotation());
        }
    }

    /**
     * Reproduz uma animação no modelo do monster.
     */
    public void playAnimation(Monster monster, String animationName) {
        Spatial model = rootNode.getChild(monster.getId());
        if (model != null) {
            AnimComposer composer = model.getControl(AnimComposer.class);
            if (composer != null) {
                composer.setCurrentAction(animationName);
            }
        }
    }

    /**
     * Registra o modelo do monster na cena.
     */
    public void registerMonsterModel(Monster monster, Spatial model) {
        model.setName(monster.getId()); // garante lookup pelo ID
        rootNode.attachChild(model);
    }

    /**
     * Remove o modelo do monster da cena.
     */
    public void removeMonster(Monster monster) {
        Spatial model = rootNode.getChild(monster.getId());
        if (model != null) {
            rootNode.detachChild(model);
        }
    }
}
