package me.brzeph.app.systems.impl;

import com.jme3.renderer.ViewPort;
import me.brzeph.app.systems.SystemAbs;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.chat.ChatMessage;
import me.brzeph.core.domain.chat.ChatTransport;
import me.brzeph.app.service.LocalLoopbackChatTransport;
import me.brzeph.infra.events.chat.ChatBroadcast;
import me.brzeph.infra.events.chat.ChatScroll;
import me.brzeph.infra.events.chat.ChatSendRequest;
import me.brzeph.infra.events.chat.ChatToggle;
import me.brzeph.infra.jme.adapter.renderer.GUIRenderAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatSystem extends SystemAbs {
    private final PlayerSystem playerSystem;
    private final GUIRenderAdapter ui;
    private final ChatTransport transport;

    private final ArrayList<ChatMessage> history = new ArrayList<>();
    private boolean open = true;
    private int scroll = 0; // 0 = últimas linhas; aumenta ao rolar p/ cima

    public ChatSystem() {
//        ui = ((GUISystem) getSystem(GUISystem.class)).getUi();
        ui = new GUIRenderAdapter(getApp());
        playerSystem = (PlayerSystem) getSystem(PlayerSystem.class);
        transport =  new LocalLoopbackChatTransport(
                () -> playerSystem.getPlayer().getName(),
                () -> playerSystem.getPlayer().getPosition(),
                () -> List.of(playerSystem.getPlayer()), // hoje 1 player, amanhã N, trocar para lista de players global
                100f // raio LOCAL, metros
        );
        transport.setOnBroadcast(this::onBroadcast);
        initialize();
    }

    public void initialize() {
        ui.buildChatPanel();
        ui.chatSetOpen(true);
        ui.chatSetMessages(List.of());
    }

    public void chatToggle(ChatToggle chatToggle) {
        toggleOpen();
//            playerSystem.inventoryJustToggled();

    }

    public void chatScroll(ChatScroll chatScroll) {
//        if (chatScroll.direction() == InputAction.Direction.UP){
//            scrollUp(3);
//        } else {
//            scrollDown(3);
//        }
    }

    public void update(float tpf) {
        ui.updateIfViewportChanged();
    }

    // === API p/ UI/Input ===
    public void toggleOpen() {
//        open = !open;
//        ui.chatSetOpen(open);
//        if (open) ui.chatSetMessages(visible());
    }

    public void scrollUp(int lines) {
        scroll += Math.max(1, lines);
        ui.chatSetMessages(visible());
    }

    public void scrollDown(int lines) {
        scroll = Math.max(0, scroll - Math.max(1, lines));
        ui.chatSetMessages(visible());
    }

    public void send(ChatChannel ch, String to, String text) {
        text = text == null ? "" : text.strip();
        if (text.isEmpty()) return;
        transport.send(new ChatSendRequest(ch, to, text));
        // (Opcional) você pode também postar no bus: bus.post(new ChatSendRequest(...));
    }

    // === Callback do transport ===
    private void onBroadcast(ChatBroadcast b) {
        history.add(b.message());
//        if (bus != null) bus.post(b);
        if (open) ui.chatSetMessages(visible());
        else      ui.chatFlash(); // badge/efeito opcional
    }

    private List<ChatMessage> visible() {
        int max = Math.max(1, ui.chatMaxLines());
        int n = history.size();
        int from = Math.max(0, n - max - scroll);
        int to   = Math.max(0, n - scroll);
        return history.subList(from, to);
    }
}

