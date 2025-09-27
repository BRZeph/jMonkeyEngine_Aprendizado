package me.brzeph.app.systems.impl.animationSystem;

public class AnimationStats {
    public AnimationType currentAnimation;

    public AnimationType getCurrentAnimation() {
        return currentAnimation;
    }

    public void setCurrentAnimation(AnimationType currentAnimation) {
        this.currentAnimation = currentAnimation;
    }
}
