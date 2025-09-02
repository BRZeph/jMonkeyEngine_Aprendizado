package me.brzeph.infra.events.chat;


import me.brzeph.core.domain.chat.ChatChannel;
import me.brzeph.core.domain.chat.ChatMessage;

public record ChatSendRequest(ChatChannel channel, String to, String text) {}
