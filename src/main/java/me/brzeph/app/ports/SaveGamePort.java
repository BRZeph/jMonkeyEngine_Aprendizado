package me.brzeph.app.ports;

public interface SaveGamePort {
    void save(Object snapshot);
    <T> T load(Class<T> type);
}
