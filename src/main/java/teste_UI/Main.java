package teste_UI;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.style.*;
import com.simsilica.lemur.component.*;

import java.awt.*;

public class Main extends SimpleApplication {

    private Label healthLabel;
    private int playerHealth = 100;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Inicializa o Lemur
        GuiGlobals.initialize(this);

        // Adiciona a barra de vida
        addHealthBar();

        // Adiciona a hotbar
        addHotbar();

        // Adiciona o inventário
        addInventory();
    }

    private void addHealthBar() {
        // Cria o Label para a barra de vida
        healthLabel = new Label("Vida: " + playerHealth + "%");
        healthLabel.setFontSize(30); // Tamanho da fonte
        healthLabel.setColor(ColorRGBA.White); // Cor do texto
        healthLabel.setLocalTranslation(10, settings.getHeight() - 40, 0); // Posição na tela
        guiNode.attachChild(healthLabel); // Adiciona na tela
    }

    private void addHotbar() {
        // Cria um painel para a hotbar
        Container hotbar = new Container(new BorderLayout());
        hotbar.setLocalTranslation(10, settings.getHeight() - 100, 0); // Posiciona a hotbar
        hotbar.setBackground(new QuadBackgroundComponent(ColorRGBA.Gray)); // Cor de fundo

        // Criar botões para os itens da hotbar
        for (int i = 0; i < 5; i++) {
            Button button = new Button("Item " + (i + 1)); // Nome do item
            button.setPreferredSize(new Vector3f(60, 60, 0)); // Tamanho do botão
            hotbar.addChild(button); // Adiciona o botão ao painel
        }

        guiNode.attachChild(hotbar); // Adiciona o painel da hotbar à interface
    }

    private void addInventory() {
        // Cria um painel para o inventário com GridLayout
        Container inventory = new Container();
        inventory.setLocalTranslation(settings.getWidth() - 320, settings.getHeight() - 350, 0); // Posição na tela
        inventory.setBackground(new QuadBackgroundComponent(ColorRGBA.DarkGray)); // Cor de fundo
//        inventory.setLayout(new GridLayout(3, 2)); // Usando GridLayout do Lemur (não AWT)

        // Adiciona botões como itens do inventário
        for (int i = 0; i < 6; i++) {
            Button itemButton = new Button("Item " + (i + 1));
            itemButton.setPreferredSize(new Vector3f(50, 50, 0)); // Tamanho do botão
            inventory.addChild(itemButton); // Adiciona o botão ao painel do inventário
        }

        guiNode.attachChild(inventory); // Adiciona o painel do inventário à interface
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Atualiza a barra de vida (simulando perda de vida)
        if (playerHealth > 0) {
            playerHealth -= 1; // Simula uma perda de vida
            healthLabel.setText("Vida: " + playerHealth + "%"); // Atualiza o texto
        }
    }
}
