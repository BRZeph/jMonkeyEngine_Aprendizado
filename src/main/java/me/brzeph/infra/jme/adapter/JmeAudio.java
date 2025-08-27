package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;

public class JmeAudio {
    private final Application app;
    private AudioNode currentMusic;

    public JmeAudio(Application app) {
        this.app = app;
    }

    public void playSfx(String assetPath, Vector3f at) {
        AudioNode sfx = new AudioNode(app.getAssetManager(), assetPath, false);
        sfx.setPositional(true);
        sfx.setLocalTranslation(at != null ? at : Vector3f.ZERO);
        sfx.setVolume(1.0f);
        ((com.jme3.app.SimpleApplication) app).getRootNode().attachChild(sfx);
        sfx.play();
    }

    public void playMusic(String assetPath, boolean loop, float volume) {
        stopMusic();
        currentMusic = new AudioNode(app.getAssetManager(), assetPath, true);
        currentMusic.setPositional(false);
        currentMusic.setLooping(loop);
        currentMusic.setVolume(volume);
        ((com.jme3.app.SimpleApplication) app).getRootNode().attachChild(currentMusic);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.removeFromParent();
            currentMusic = null;
        }
    }
}