package me.brzeph.events;

import com.jme3.app.state.BaseAppState;

public record NavigateToEvent(Class<? extends BaseAppState> stateType) {}

