package me.brzeph.app.ports;

import com.jme3.math.Vector3f;

public interface Audio {
    void playSfx(String assetPath, Vector3f at);
    void playMusic(String assetPath, boolean loop, float volume);
    void stopMusic();
}
