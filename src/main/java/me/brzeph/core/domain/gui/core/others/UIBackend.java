package me.brzeph.core.domain.gui.core.others;

public interface UIBackend {
    void drawRect(Rect r, Color color, float cornerRadius);
    void drawImage(Object imageHandle, Rect r);
    void drawText(String text, float x, float y, UIFont font, Color color);
    float textWidth(String text, UIFont font);
    float textLineHeight(UIFont font);
}
