package me.brzeph.infra.jme.adapter.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.core.domain.entity.enemies.Monster;

public class MonsterAudioAdapter {

    private final AssetManager assetManager;

    public MonsterAudioAdapter(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Toca um som simples para o monster.
     */
    public void playSound(String soundFile) {
        AudioNode audio = new AudioNode(assetManager, "assets/sounds/entities/monsters/" + soundFile + ".wav", AudioData.DataType.Buffer);
        audio.setPositional(false); // 2D sound (global)
        audio.setLooping(false);
        audio.play();
    }

    /**
     * Toca som posicional na posição do player.
     */
    public void playSoundAt(Monster monster, String soundFile) {
        AudioNode audio = new AudioNode(assetManager, "assets/sounds/entities/monsters/" + soundFile + ".wav", AudioData.DataType.Buffer);
        audio.setPositional(true);
        audio.setLocalTranslation(monster.getPosition());
        audio.setLooping(false);
        audio.play();
    }
}
