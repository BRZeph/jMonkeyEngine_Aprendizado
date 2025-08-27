package me.brzeph.infra.jme.adapter.renderer;

import com.jme3.anim.AnimComposer;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import me.brzeph.core.domain.entity.Player;

public class JmePlayerRenderer {

    private final Node rootNode;

    public JmePlayerRenderer(Node rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Atualiza posição/rotação do modelo 3D do player.
     */
    public void updatePosition(Player player) {
        Spatial model = rootNode.getChild(player.getId());
        if (model != null) {
            model.setLocalTranslation(player.getPosition());
            model.setLocalRotation(player.getRotation());
        }
    }

    /**
     * Reproduz uma animação no modelo do player.
     */
    public void playAnimation(Player player, String animationName) {
        Spatial model = rootNode.getChild(player.getId());
        if (model != null) {
            AnimComposer composer = model.getControl(AnimComposer.class);
            if (composer != null) {
                composer.setCurrentAction(animationName);
            }
        }
    }

    /**
     * Registra o modelo do player na cena.
     */
    public void registerPlayerModel(Player player, Spatial model) {
        model.setName(player.getId()); // garante lookup pelo ID
        rootNode.attachChild(model);
    }

    /**
     * Remove o modelo do player da cena.
     */
    public void removePlayer(Player player) {
        Spatial model = rootNode.getChild(player.getId());
        if (model != null) {
            rootNode.detachChild(model);
        }
    }
}
