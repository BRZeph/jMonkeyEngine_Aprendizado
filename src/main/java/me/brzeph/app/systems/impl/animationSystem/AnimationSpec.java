package me.brzeph.app.systems.impl.animationSystem;

import com.jme3.anim.AnimClip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnimationSpec {
    private final String animationPath;

    public AnimationSpec(String animationPath) {
        this.animationPath = animationPath;
    }

    public String getAnimationPath() {
        return animationPath;
    }
}
