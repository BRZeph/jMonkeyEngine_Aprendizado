package me.brzeph.infra.persistence;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.font.BitmapFont;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repositório de assets com um preload simples e tolerante a falhas.
 * Você pode registrar aqui tudo que deve estar em cache quando o jogo abre.
 */
public class AssetRepositoryImpl {

    private static final Logger log = Logger.getLogger(AssetRepositoryImpl.class.getName());

    private final AssetManager assets;

    public AssetRepositoryImpl(AssetManager assets) {
        this.assets = assets;
    }

    public void preload() {
        // Adapte esta lista aos seus assets reais:
        var fonts = List.of(
                "Interface/Fonts/Default.fnt"   // jME default
//                "Interface/Fonts/Orbitron.fnt"
        );
        var mats = List.of(
                "Common/MatDefs/Misc/Unshaded.j3md"
//                "Materials/MyPBR.j3md"
        );
        var models = List.of(
//                 "Models/Player/Player.gltf",
//                 "Models/UI/Cursor.j3o"
        );
        var sounds = List.of(
//                 "Sounds/ui_click.ogg",
//                 "Sounds/music_theme.ogg"
        );

        List<String> ok = new ArrayList<>();
        List<String> fail = new ArrayList<>();

        // Fonts
        for (String f : fonts) {
            try {
                BitmapFont font = assets.loadFont(f);
                if (font != null) ok.add("FONT:" + f); else fail.add("FONT:" + f);
            } catch (Exception ex) { fail.add("FONT:" + f + " -> " + ex.getMessage()); }
        }

        // Materials (só garantir que resolve)
        for (String m : mats) {
            try {
                Material mat = new Material(assets, m);
                if (mat != null) ok.add("MAT:" + m); else fail.add("MAT:" + m);
            } catch (Exception ex) { fail.add("MAT:" + m + " -> " + ex.getMessage()); }
        }

        // Models (apenas cache)
//        for (String mdl : models) {
//            try {
//                Spatial s = assets.loadModel(mdl);
//                if (s != null) ok.add("MODEL:" + mdl); else fail.add("MODEL:" + mdl);
//            } catch (Exception ex) { fail.add("MODEL:" + mdl + " -> " + ex.getMessage()); }
//        }

        // Sounds (carrega como raw, a API concreta fica no adapter de áudio)
//        for (String snd : sounds) {
//            try {
//                assets.loadAudio(snd);
//                ok.add("SND:" + snd);
//            } catch (Exception ex) { fail.add("SND:" + snd + " -> " + ex.getMessage()); }
//        }

        log.info(() -> "[Assets PRELOAD] OK=" + ok.size() + " FAIL=" + fail.size());
        if (!fail.isEmpty()) {
            for (String f : fail) log.warning(() -> " - Falhou: " + f);
        }
    }
}
