package me.brzeph.core.domain.chat;


import me.brzeph.infra.events.chat.ChatBroadcast;
import me.brzeph.infra.events.chat.ChatSendRequest;

import java.util.function.Consumer;

public interface ChatTransport {
    void send(ChatSendRequest req);             // cliente → (transport) → roteamento
    void setOnBroadcast(Consumer<ChatBroadcast> cb); // callback p/ entregar msgs
}
