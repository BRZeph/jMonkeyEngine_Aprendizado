package me.brzeph.domain.entity.item;

import java.util.UUID; /** Identidade única de instância (o item que o jogador possui) */
public record ItemId(UUID value) {
    public static ItemId newId() { return new ItemId(UUID.randomUUID()); }
}
