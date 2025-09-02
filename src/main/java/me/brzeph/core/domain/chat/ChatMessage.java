package me.brzeph.core.domain.chat;

public record ChatMessage(
        ChatChannel channel,
        String from,            // "PlayerA"
        String to,              // p/ whisper; null caso contrário
        String text,
        long   timestampMillis
) {}
