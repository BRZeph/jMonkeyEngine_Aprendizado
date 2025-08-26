package me.brzeph.infra.jme.adapter;

import com.jme3.app.Application;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import me.brzeph.app.ports.Audio;

public class JmeAudio implements Audio {
    private final Application app;
    private AudioNode currentMusic;

    public JmeAudio(Application app) {
        this.app = app;
    }

    @Override
    public void playSfx(String assetPath, Vector3f at) {
        AudioNode sfx = new AudioNode(app.getAssetManager(), assetPath, false);
        sfx.setPositional(true);
        sfx.setLocalTranslation(at != null ? at : Vector3f.ZERO);
        sfx.setVolume(1.0f);
        ((com.jme3.app.SimpleApplication) app).getRootNode().attachChild(sfx);
        sfx.play();
    }

    @Override
    public void playMusic(String assetPath, boolean loop, float volume) {
        stopMusic();
        currentMusic = new AudioNode(app.getAssetManager(), assetPath, true);
        currentMusic.setPositional(false);
        currentMusic.setLooping(loop);
        currentMusic.setVolume(volume);
        ((com.jme3.app.SimpleApplication) app).getRootNode().attachChild(currentMusic);
        currentMusic.play();
    }

    @Override
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.removeFromParent();
            currentMusic = null;
        }
    }
}