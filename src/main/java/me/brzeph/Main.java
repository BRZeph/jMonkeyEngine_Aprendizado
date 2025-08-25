package me.brzeph;

import com.jme3.system.AppSettings;

public class Main {
    /*
    Por enquanto vou socar tudo no Main e boa, quando terminar o aprendizado vou reescrever tudo e criar coisas como
    ScreenManager e tudo mais.
     */

    public static void main(String[] args) {
        Test_01 app = new Test_01();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("JM - Hello World");
        settings.setResolution(1280, 720);
        settings.setVSync(true);
        app.setSettings(settings); // Tem que ser chamado antes do app.start().

        app.start();
    }
/*
    Sobre VSync:
        -> Se o monitor é 60 Hz → o jogo limita o FPS a 60.
        -> Se o monitor é 144 Hz → o jogo limita o FPS a 144.
 */
}