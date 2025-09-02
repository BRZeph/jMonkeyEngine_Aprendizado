package me.brzeph.infra.jme.adapter.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import me.brzeph.core.domain.entity.Player;

public class PlayerAudioAdapter {

    private final AssetManager assetManager;

    public PlayerAudioAdapter(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Toca um som simples para o player.
     */
    public void playSound(String soundFile) {
        AudioNode audio = new AudioNode(assetManager, "assets/sounds/entities/" + soundFile + ".wav", AudioData.DataType.Buffer);
        audio.setPositional(false); // 2D sound (global)
        audio.setLooping(false);
        audio.play();
    }

    /**
     * Toca som posicional na posição do player.
     */
    public void playSoundAt(Player player, String soundFile) {
        AudioNode audio = new AudioNode(assetManager, "assets/sounds/entities/" + soundFile + ".wav", AudioData.DataType.Buffer);
        audio.setPositional(true);
        audio.setLocalTranslation(player.getPosition());
        audio.setLooping(false);
        audio.play();
    }
}

