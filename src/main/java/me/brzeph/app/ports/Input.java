package me.brzeph.app.ports;

import me.brzeph.infra.events.EventBus;

public interface Input {
    void bindGameplayMappings(EventBus bus);
}