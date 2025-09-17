package me.brzeph.core.domain.gui.core.screens;

import me.brzeph.core.domain.entity.item.InventoryItem;
import me.brzeph.core.domain.entity.item.ItemInstance;
import me.brzeph.core.domain.gui.core.events.*;
import me.brzeph.core.domain.gui.core.widgets.UIInventorySlot;
import me.brzeph.core.domain.gui.core.widgets.Widget;
import me.brzeph.infra.events.EventBus;

import java.util.Optional;

/**
 * Roteia input para o ScreenManager:
 * - respeita ordem topo→fundo
 * - suporta telas “modais” (freezeInputBehind) que bloqueiam input das de trás
 * - publica OnClickEvent/OnHoldEvent (legado) e também UIScreenClickEvent/UIScreenHoldEvent (com screenId)
 */
public final class ScreenManagerInput {
    private final ScreenManager mgr;
    private final EventBus bus;
    private final float holdThreshold = 0.45f;

    // --- NOVO: parâmetros de drag ---
    private final float dragThresholdPx = 6f; // distância antes de considerar que virou drag

    private boolean isDown = false;
    private float heldTime = 0f;
    private boolean holdSent = false;

    private Screen targetScreen = null;
    private Widget<?> targetWidget = null;

    // --- NOVO: estado de drag ---
    private boolean dragging = false;
    private float startX, startY;
    private float prevX, prevY;
    private int   pressedButton = 0;

    private ItemInstance holdingItem = null;

    public ScreenManagerInput(ScreenManager mgr, EventBus bus){
        this.mgr = mgr; this.bus = bus;
    }

    public void update(float dt, float mouseX, float mouseY, boolean mouseDown, int button){
        if (mouseDown && !isDown){
            // PRESS
            targetScreen = pickScreen(mouseX, mouseY);
            targetWidget = (targetScreen != null) ? targetScreen.hit(mouseX, mouseY) : null;

            isDown = true;
            heldTime = 0f;
            holdSent = false;
            dragging = false;
            startX = prevX = mouseX;
            startY = prevY = mouseY;
            pressedButton = button;

            if (targetScreen != null && targetWidget != null){
                if (targetWidget instanceof UIInventorySlot){
                    InventoryItem item = ((UIInventorySlot) targetWidget).getItem();
                    bus.post(new UIScreenClickEvent(targetScreen.id(), targetWidget.id(), mouseX, mouseY, button, item));
                }
            }
        }
        else if (mouseDown){
            // HELD
            heldTime += dt;

            // --- NOVO: detectar início do drag por distância ---
            if (!dragging){
                float dx0 = mouseX - startX;
                float dy0 = mouseY - startY;
                if ((dx0*dx0 + dy0*dy0) >= (dragThresholdPx * dragThresholdPx)
                        && targetScreen != null && targetWidget != null){
                    dragging = true;
                    if (targetWidget instanceof UIInventorySlot) {
                        bus.post(new UIDragStartEvent(targetScreen.id(), (UIInventorySlot) targetWidget, startX, startY, pressedButton,
                                ((UIInventorySlot) targetWidget).getItem())
                        );
                    }
                }
            }

            if (dragging){
                float dx = mouseX - prevX;
                float dy = mouseY - prevY;
                if ((dx != 0f || dy != 0f) && targetScreen != null && targetWidget != null){
                    if (targetWidget instanceof UIInventorySlot) {
                        bus.post(new UIDragMoveEvent(targetScreen.id(), targetWidget.id(), mouseX, mouseY, dx, dy,
                                ((UIInventorySlot) targetWidget).getItem()));
                    }
                }
            } else if (!holdSent && heldTime >= holdThreshold && targetScreen != null && targetWidget != null){
                // hold (se ainda não virou drag)
                bus.post(new OnHoldEvent(targetWidget.id(), mouseX, mouseY, heldTime));
                bus.post(new UIScreenHoldEvent(targetScreen.id(), targetWidget.id(), mouseX, mouseY, heldTime));
                holdSent = true;
            }

            prevX = mouseX; prevY = mouseY;
        }
        else if (isDown){
            // RELEASE
            if (dragging && targetScreen != null && targetWidget != null && targetWidget instanceof UIInventorySlot){
                bus.post(new UIDragEndEvent(targetScreen.id(), targetWidget.id(), mouseX, mouseY, pressedButton));
            }

            isDown = false;
            dragging = false;
            targetScreen = null;
            targetWidget = null;
            heldTime = 0f;
            holdSent = false;
        }
    }

    private Screen pickScreen(float x, float y){
        Optional<Screen> topModal = mgr.topMostModal();
        if (topModal.isPresent()){
            // Modal no topo bloqueia o resto. Ainda assim, só haverá evento se o hit achar um widget.
            Screen s = topModal.get();
            return (s.isActive() && s.isVisible()) ? s : null;
        }
        for (Screen s : mgr.orderedTopFirst()){
            if (!s.isActive() || !s.isVisible()) continue;
            if (s.hit(x, y) != null) return s;
        }
        return null;
    }
}

