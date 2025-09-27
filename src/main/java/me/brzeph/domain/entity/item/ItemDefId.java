package me.brzeph.domain.entity.item;

import java.util.*;

/** Identidade estável de definição (catálogo) */
public record ItemDefId(String dbId, String itemId) {
    public static ItemDefId of(String v) {
        return new ItemDefId(Objects.requireNonNull(v), v);
    }
    /*
    dbId -> unique id for database storage. dbId = [fixedId]_[counter].
    itemId -> id for each grouping items such as "Crafting Material". itemId = [fixedId].
     */
}

