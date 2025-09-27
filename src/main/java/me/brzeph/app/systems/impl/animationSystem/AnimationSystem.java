package me.brzeph.app.systems.impl.animationSystem;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.scene.Node;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.domain.entity.CharacterEntity;
import me.brzeph.domain.entity.EntityType;
import me.brzeph.domain.entity.GameEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.brzeph.app.systems.impl.animationSystem.AnimationService.dumpScene;
import static me.brzeph.app.systems.impl.animationSystem.AnimationService.findControl;

public class AnimationSystem extends SystemAbs {
    private final HashMap<EntityType, List<Animation>> animations;
    private final HashMap<EntityType, Node> composers;
    private Node currentAnimComposerModel;

    public AnimationSystem() {
        animations = new HashMap<>();
        composers = new HashMap<>();
    }

    public void initialize(){
        for (EntityType type : EntityType.values()) {
            if (type.blueprint().anim() == null){
                animations.put(type, new ArrayList<>());
            } else {
                animations.put(type, findAnimClip(type.blueprint().anim().getAnimationPath()));

                assert currentAnimComposerModel != null;
                composers.put(type, currentAnimComposerModel);
                currentAnimComposerModel = null;
            }
        }
    }

    public void switchAnimation(CharacterEntity e, AnimationType t){
        AnimComposer ac = e.getAnimComposer();

        AnimationType type = ac.hasAnimClip(t.name()) ? t : AnimationType.Idle_Parado;

        e.getAnimStats().setCurrentAnimation(type);

        if (t.shouldLoop()) {
            ac.setCurrentAction(type.name(), AnimComposer.DEFAULT_LAYER, true);
        } else {
            ac.setCurrentAction(type.name());
        }
    }

    public AnimationType getCurrentAnimation(CharacterEntity e){
        return e.getAnimStats().getCurrentAnimation();
    }

    public Node getNode(EntityType t){
        return copyModel(composers.get(t));
    }

    public AnimComposer getComposer(EntityType type) {
        return findControl(composers.get(type), AnimComposer.class);
    }

    private Node copyModel(Node n){
        return (Node) n.clone();
    }

    private List<Animation> findAnimClip(String path) {
        if (path.isEmpty()) return null;

        Node model = (Node) getAssetManager().loadModel(path);
        AnimComposer ac = findControl(model, AnimComposer.class);

        if (ac == null) {
            dumpScene(model, "");
            throw new IllegalStateException("AnimComposer não encontrado no modelo: " + path);
        }

        List<AnimClip> found = new ArrayList<>(ac.getAnimClips());

        if (found.isEmpty()){
            StringBuilder names = new StringBuilder();
            for (AnimClip c : ac.getAnimClips()) {
                if (!names.isEmpty()) names.append(", ");
                names.append(c.getName());
            }
            throw new IllegalStateException("Path '" + path + "' não encontrado." +
                    " Clipes disponíveis: [" + names + "]");
        }

        List<Animation> animations = new ArrayList<>();

        for (AnimClip c : found) {
            animations.add(new Animation(
                    AnimationType.valueOf(c.getName()), c
            ));
        }

        currentAnimComposerModel = model;

        return animations;
    }

    @Override
    public void update(float tpf) {

    }
}
