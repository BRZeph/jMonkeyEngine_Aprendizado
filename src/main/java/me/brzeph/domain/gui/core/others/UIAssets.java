package me.brzeph.domain.gui.core.others;

import java.util.Optional;

public interface UIAssets {
    Optional<Object> image(String path);
    Optional<UIFont> font(String path);
}
