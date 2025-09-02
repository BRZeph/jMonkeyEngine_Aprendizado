package me.brzeph.infra.events.chat;

import me.brzeph.core.domain.chat.ChatMessage;

public record ChatBroadcast(ChatMessage message) {}
