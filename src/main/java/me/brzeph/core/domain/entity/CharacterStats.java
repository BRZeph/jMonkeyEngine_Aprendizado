package me.brzeph.core.domain.entity;

public class CharacterStats {
    private int level;
    private int hp;
    private int mp;
    private float speed;
    private float maxSeeDistance;
    private float basicAttackRange;
    private boolean running;
    private boolean idle;

    public CharacterStats(int level, int hp, int mp, float speed, float maxSeeDistance, float basicAttackRange) {
        this.level = level;
        this.hp = hp;
        this.mp = mp;
        this.speed = speed;
        this.maxSeeDistance = maxSeeDistance;
        this.basicAttackRange = basicAttackRange;
        this.running = false;
        this.idle = true;
    }

    public boolean isIdle() {
        return idle;
    }

    public void toggleIdle(){
        this.idle = !this.idle;
    }

    public void applySpeedModifier(float buff){
        speed *= buff;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void toggleRunning(){
        this.running = !running;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public float getMaxSeeDistance() {
        return maxSeeDistance;
    }

    public void setMaxSeeDistance(float maxSeeDistance) {
        this.maxSeeDistance = maxSeeDistance;
    }

    public float getBasicAttackRange() {
        return basicAttackRange;
    }

    public void setBasicAttackRange(float basicAttackRange) {
        this.basicAttackRange = basicAttackRange;
    }
}
