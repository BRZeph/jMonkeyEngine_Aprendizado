package me.brzeph.core.domain.entity.item;

import java.util.*;

/** Identidade estável de definição (catálogo) */
public record ItemDefId(String value) {
    public static ItemDefId of(String v) { return new ItemDefId(Objects.requireNonNull(v)); }
}

