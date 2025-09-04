package me.brzeph.infra.jme.adapter.renderer;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.chat.ChatMessage;
import me.brzeph.infra.jme.adapter.JmeRender;

public class GUIRenderAdapter{

    private final Node guiRoot;
    private final AssetManager assets;
    private final SimpleApplication app;
    // ==== CHAT ====
    private Node chatPanel;
    private Node chatLines;            // container das linhas do chat
    private BitmapFont chatFont;
    private float chatLineHeight;
    private int cachedChatMaxLines = 0;

    // layers
    private final Node hudLayer = new Node("HUD_LAYER");         // hotbar + minimap
    private final Node overlayLayer = new Node("OVERLAY_LAYER"); // inventory

    // widgets
    private Node hotbar;
    private Node minimap;
    private Node inventory;

    // cache resolução p/ responder resize
    private int cachedW = -1, cachedH = -1;

    public GUIRenderAdapter(SimpleApplication app) { // Talvez registrar toda a UI análogamente ao PLAYER_[counter].
        this.app = app;
        this.guiRoot = app.getGuiNode();
        this.assets   = app.getAssetManager();
    }

    public void buildStaticUI() {
        // layers no guiRoot
        if (hudLayer.getParent() == null)  guiRoot.attachChild(hudLayer);
        if (overlayLayer.getParent() == null) guiRoot.attachChild(overlayLayer);
        hudLayer.setQueueBucket(RenderQueue.Bucket.Gui);
        overlayLayer.setQueueBucket(RenderQueue.Bucket.Gui);

        // cria widgets
        createHotbar(8);          // 8 slots (exemplo)
        createMinimap();               // quadrado preto com borda branca
        createInventory();             // painel central

        layout();                      // posiciona conforme viewport
    }

    public void updateIfViewportChanged() {
        int w = app.getCamera().getWidth();
        int h = app.getCamera().getHeight();
        if (w != cachedW || h != cachedH) layout();
    }

    /* ===================== Visibility ===================== */

    public void setHotbarVisible(boolean visible) {
        hotbar.setCullHint(visible ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
    }

    public void setMinimapVisible(boolean visible) {
        minimap.setCullHint(visible ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
    }

    public void setInventoryVisible(boolean visible) {
        overlayLayer.setCullHint(visible ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
    }

    /* ===================== Layout ===================== */

    private void layout() {
        int w = app.getCamera().getWidth();
        int h = app.getCamera().getHeight();
        cachedW = w; cachedH = h;

        float scale = h / 1080f;        // escala simples baseada na altura
        float margin = 16f * scale;

        // HOTBAR: centralizado embaixo
        if (hotbar != null) {
            float hbW = 600f * scale;
            float hbH = 80f  * scale;
            positionRect(hotbar, (w - hbW)/2f, margin, hbW, hbH);
        }

        // MINIMAP: topo-direita (novo)
        if (minimap != null) {
            float mmSize = 160f * scale;
            float x = w - margin - mmSize;  // direita
            float y = h - margin - mmSize;  // topo (origem do guiNode é bottom-left)
            positionRect(minimap, x, y, mmSize, mmSize);
        }

        // INVENTORY: central (overlay)
        if (inventory != null) {
            float invW = Math.min(800f * scale, w - 2*margin);
            float invH = Math.min(600f * scale, h - 2*margin);
            positionRect(inventory, (w - invW)/2f, (h - invH)/2f, invW, invH);
        }

        // CHAT: canto inferior esquerdo, acima da hotbar (ajuste como preferir)
        if (chatPanel != null) {
            float chatScale = app.getCamera().getHeight() / 1080f;
            float chatMargin = 16f * chatScale;

            float chatW = 520f * chatScale;
            float chatH = 220f * chatScale;

            float x = chatMargin;
            float y = chatMargin + 8f * chatScale;
            if (hotbar != null) {
                float hbH = ((Quad)((Geometry)hotbar.getChild("fill")).getMesh()).getHeight();
                y += hbH + 8f * chatScale; // deixar um espaço acima da hotbar
            }

            positionRect(chatPanel, x, y, chatW, chatH);

            // quantas linhas cabem na altura atual
            cachedChatMaxLines = Math.max(1, (int) Math.floor((chatH - 10f*chatScale) / chatLineHeight));
        }

    }

    private void positionRect(Node panel, float x, float y, float w, float h) {
        Geometry fill = (Geometry) panel.getChild("fill");
        Node border   = (Node) panel.getChild("border");

        ((Quad) fill.getMesh()).updateGeometry(w, h);
        rebuildBorder(border, w, h);
        rebuildSeparators(panel, w, h);

        panel.setLocalTranslation(x, y, 0);
    }

    /* ===================== Widgets ===================== */

    private void createHotbar(int slots) {
        if (hotbar != null) hotbar.removeFromParent();
        hotbar = buildPanel("HOTBAR", new ColorRGBA(0,0,0,0.4f), ColorRGBA.White, 2f);

        // armazena quantos slots para redesenhar no layout
        hotbar.setUserData("slotCount", slots);

        // garante que exista o nó "separators" dentro de border (não será apagado no rebuildBorder)
        Node border = (Node) hotbar.getChild("border");
        if (border.getChild("separators") == null) {
            border.attachChild(new Node("separators"));
        }

        hudLayer.attachChild(hotbar);
    }

    private void createMinimap() {
        if (minimap != null) minimap.removeFromParent();
        minimap = buildPanel("MINIMAP", ColorRGBA.Black, ColorRGBA.White, 2f);
        hudLayer.attachChild(minimap);
    }

    private void createInventory() {
        if (inventory != null) inventory.removeFromParent();
        // overlay escuro translúcido + painel central
        inventory = buildPanel("INVENTORY", new ColorRGBA(0,0,0,0.6f), ColorRGBA.White, 3f);
        overlayLayer.attachChild(inventory);
    }

    /* ===================== Panel builder ===================== */

    private Node buildPanel(String name, ColorRGBA fillColor, ColorRGBA borderColor, float borderThickness) {
        Node panel = new Node(name);

        Geometry fill = quad("fill", fillColor);   // <<-- "fill" minúsculo
        panel.attachChild(fill);

        Node border = new Node("border");
        border.setUserData("borderColor", borderColor);
        border.setUserData("borderThickness", borderThickness);
        panel.attachChild(border);

        ((Quad) fill.getMesh()).updateGeometry(100, 100);
        rebuildBorder(border, 100, 100);

        return panel;
    }

    private Geometry quad(String name, ColorRGBA color) {
        Quad quad = new Quad(100, 100);
        Geometry g = new Geometry(name, quad);

        Material m = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color); // use alpha < 1f p/ ver transparência (ex.: new ColorRGBA(0,0,0,0.4f))
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setMaterial(m);
        g.setQueueBucket(RenderQueue.Bucket.Gui); // GUI renderiza em 2D; ordem = de anexação
        return g;
    }

    private void rebuildBorder(Node border, float w, float h) {
        ColorRGBA color = border.getUserData("borderColor");
        Float thick = border.getUserData("borderThickness");
        float t = thick != null ? thick : 2f;

        Node rect = (Node) border.getChild("rect");
        if (rect == null) {
            rect = new Node("rect");
            border.attachChild(rect);
        }
        rect.detachAllChildren(); // só a moldura!

        rect.attachChild(line(new float[]{0,0, w,0}, color, t));
        rect.attachChild(line(new float[]{w,0, w,h}, color, t));
        rect.attachChild(line(new float[]{w,h, 0,h}, color, t));
        rect.attachChild(line(new float[]{0,h, 0,0}, color, t));
    }

    private void rebuildSeparators(Node panel, float w, float h) {
        Integer slots = panel.getUserData("slotCount");
        if (slots == null || slots <= 1) return; // não é hotbar, ou sem divisórias

        Node border = (Node) panel.getChild("border");
        Node seps = (Node) border.getChild("separators");
        if (seps == null) {
            seps = new Node("separators");
            border.attachChild(seps);
        }
        seps.detachAllChildren();

        float slotW = w / slots;
        for (int i = 1; i < slots; i++) {
            float x = i * slotW;
            seps.attachChild(line(new float[]{x, 0, x, h}, ColorRGBA.White, 1.5f));
        }
    }

    private Geometry line(float[] xy, ColorRGBA color, float width) {
        Line ln = new Line(
                new com.jme3.math.Vector3f(xy[0], xy[1], 0),
                new com.jme3.math.Vector3f(xy[2], xy[3], 0)
        );
        ln.setLineWidth(width);
        Geometry g = new Geometry("ln", ln);
        Material m = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        g.setMaterial(m);
        g.setQueueBucket(RenderQueue.Bucket.Gui);
        return g;
    }

    // ==== CHAT ====
    // Cria painel de chat (fechado por padrão). Chame no initialize do seu ChatSystem.
    public void buildChatPanel() {
        if (chatFont == null) chatFont = assets.loadFont("Interface/Fonts/Default.fnt");
        chatLineHeight = chatFont.getCharSet().getLineHeight();

        if (chatPanel != null) chatPanel.removeFromParent();
        chatPanel = buildPanel("CHAT", new ColorRGBA(0,0,0,0.35f), ColorRGBA.White, 2f);

        chatLines = new Node("chatLines");
        chatLines.setQueueBucket(RenderQueue.Bucket.Gui);
        chatPanel.attachChild(chatLines);

        hudLayer.attachChild(chatPanel);

        // começa fechado e vazio
        chatSetOpen(false);
        chatSetMessages(java.util.Collections.emptyList());

        // posiciona conforme viewport atual
        layout();
    }

    // Mostra/oculta o painel de chat
    public void chatSetOpen(boolean open) {
        if (chatPanel == null) return;
        chatPanel.setCullHint(open ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
    }

    // Quantas linhas cabem hoje no painel (usado pelo ChatSystem para o “windowing”)
    public int chatMaxLines() {
        return Math.max(1, cachedChatMaxLines);
    }

    // Renderiza as mensagens visíveis (últimas N, já filtradas/recortadas pelo ChatSystem)
    public void chatSetMessages(java.util.List<ChatMessage> lines) {
        if (chatPanel == null || chatLines == null) return;

        // Limpa conteúdo anterior
        chatLines.detachAllChildren();

        // Área disponível
        float w = ((Quad)((Geometry)chatPanel.getChild("fill")).getMesh()).getWidth();
        float h = ((Quad)((Geometry)chatPanel.getChild("fill")).getMesh()).getHeight();
        float pad = 25f;
        float maxWidth = Math.max(1f, w - 2*pad);

        // desenha de baixo pra cima
        float y = pad;
        for (int i = 0; i < lines.size(); i++) {
            ChatMessage m = lines.get(i);
            String txt = formatLine(m);
            ColorRGBA col = colorFor(m.channel());

            BitmapText proto = new BitmapText(chatFont);
            proto.setColor(col);
            proto.setSize(chatFont.getCharSet().getRenderedSize());
            proto.setQueueBucket(RenderQueue.Bucket.Gui);

            // quebra em múltiplas linhas se necessário e anexa ao chatLines
            y = wrapInto(chatLines, proto, pad, y, maxWidth, chatLineHeight, txt);
        }
    }

    // “Ping” visual simples: muda a cor da borda por um instante (pode evoluir p/ animação)
    public void chatFlash() {
        if (chatPanel == null) return;
        Node border = (Node) chatPanel.getChild("border");
        ColorRGBA old = border.getUserData("borderColor");
        border.setUserData("borderColor_backup", old);
        // borda amarela
        border.setUserData("borderColor", ColorRGBA.Yellow);
        // agende voltar ao normal no próximo frame (~truque simples)
        app.enqueue(() -> {
            // pequeno delay
            app.enqueue(() -> {
                ColorRGBA backup = border.getUserData("borderColor_backup");
                if (backup != null) border.setUserData("borderColor", backup);
                // força redesenho da moldura
                float w = ((Quad)((Geometry)chatPanel.getChild("fill")).getMesh()).getWidth();
                float h = ((Quad)((Geometry)chatPanel.getChild("fill")).getMesh()).getHeight();
                rebuildBorder(border, w, h);
                return null;
            });
            return null;
        });
    }

    /* ---------- Helpers do Chat ---------- */

    // Quebra texto por largura e cria linhas BitmapText; retorna o próximo y livre
    private float wrapInto(Node parent, BitmapText proto,
                           float x, float baseY, float maxWidth, float lh, String text) {
        int start = 0;
        float y = baseY;

        while (start < text.length()) {
            int end = text.length();
            proto.setText(text.substring(start, end));
            // tenta quebrar em espaço até caber
            while (proto.getLineWidth() > maxWidth && end > start) {
                end = text.lastIndexOf(' ', end - 1);
                if (end <= start) {
                    // hard cut (sem espaços)
                    end = Math.min(text.length(), start + Math.max(1, (int)(maxWidth / (proto.getSize()*0.6f))));
                    break;
                }
                proto.setText(text.substring(start, end));
            }
            BitmapText line = proto.clone();
            line.setText(text.substring(start, end));
            line.setLocalTranslation(x, y, 0);
            parent.attachChild(line);

            y += lh;
            start = Math.min(end + 1, text.length());
        }
        return y;
    }

    private String formatLine(ChatMessage m) {
        // timestamp HH:mm:ss
        long millis = m.timestampMillis() % 86400000;  // Isso garante que o valor esteja dentro de 24 horas
        String time = java.time.LocalTime.ofNanoOfDay(java.time.Duration.ofMillis(millis).toNanos())
                .withNano(0)
                .toString();
        return switch (m.channel()) {
            case SERVER  -> String.format("[%s] [SERVER] %s", time, m.text());
            case GLOBAL  -> String.format("[%s] [%s] %s", time, m.from(), m.text());
            case LOCAL   -> String.format("[%s] (local) [%s] %s", time, m.from(), m.text());
            case WHISPER -> String.format("[%s] [whisper %s→%s] %s",
                    time, m.from(), m.to() == null ? "?" : m.to(), m.text());
            case PARTY   -> String.format("[%s] [party] [%s] %s", time, m.from(), m.text());
            case GUILD   -> String.format("[%s] [guild] [%s] %s", time, m.from(), m.text());
        };
    }

    private ColorRGBA colorFor(ChatChannel ch) {
        return switch (ch) {
            case GLOBAL -> ColorRGBA.Cyan;
            case LOCAL  -> ColorRGBA.White;
            case SERVER -> ColorRGBA.Yellow;
            case WHISPER-> new ColorRGBA(1f,0.6f,1f,1f);
            case PARTY  -> new ColorRGBA(0.6f,1f,0.6f,1f);
            case GUILD  -> new ColorRGBA(0.4f,1f,1f,1f);
        };
    }
}
