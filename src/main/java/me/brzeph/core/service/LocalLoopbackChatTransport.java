package me.brzeph.core.service;

import com.jme3.math.Vector3f;
import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.chat.ChatMessage;
import me.brzeph.core.domain.chat.ChatTransport;
import me.brzeph.core.domain.entity.Player;
import me.brzeph.infra.events.chat.ChatBroadcast;
import me.brzeph.infra.events.chat.ChatSendRequest;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


/** Implementação atual: tudo acontece localmente (single-player). */
public class LocalLoopbackChatTransport implements ChatTransport {
    private Consumer<ChatBroadcast> onBroadcast;
    private final Supplier<String> myName;
    private final Supplier<Vector3f> myPos;
    private final Supplier<List<Player>> players; // p/ LOCAL (multi futuro)
    private final float localRadius; // metros

    public LocalLoopbackChatTransport(
            Supplier<String> myName,
            Supplier<Vector3f> myPos,
            Supplier<List<Player>> players,
            float localRadiusMeters
    ){
        this.myName = myName;
        this.myPos = myPos;
        this.players = players;
        this.localRadius = localRadiusMeters;
    }

    @Override public void setOnBroadcast(Consumer<ChatBroadcast> cb) { this.onBroadcast = cb; }

    @Override public void send(ChatSendRequest req) {
        // “Roteamento” local: só ecoa no próprio cliente, simulando regras básicas
        String from = myName.get();
        long ts = System.currentTimeMillis();

        switch (req.channel()) {
            case GLOBAL, SERVER -> {
                emit(new ChatMessage(req.channel(), from, null, req.text(), ts));
            }
            case LOCAL -> {
                // single-player: entrega para você mesmo; se virar multiplayer local, use raio
                emit(new ChatMessage(ChatChannel.LOCAL, from, null, req.text(), ts));
            }
            case WHISPER -> {
                emit(new ChatMessage(ChatChannel.WHISPER, from, req.to(), req.text(), ts));
            }
            case PARTY, GUILD -> {
                emit(new ChatMessage(req.channel(), from, null, req.text(), ts));
            }
        }
    }

    private void emit(ChatMessage msg){
        if (onBroadcast != null) onBroadcast.accept(new ChatBroadcast(msg));
    }
}
