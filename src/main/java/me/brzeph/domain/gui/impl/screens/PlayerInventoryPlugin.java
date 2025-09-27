package me.brzeph.domain.gui.impl.screens;

import me.brzeph.domain.entity.player.Player;
import me.brzeph.domain.gui.core.layout.ColumnLayout;
import me.brzeph.domain.gui.core.layout.LayoutParams;
import me.brzeph.domain.gui.core.layout.RowLayout;
import me.brzeph.domain.gui.core.others.Align;
import me.brzeph.domain.gui.core.others.Color;
import me.brzeph.domain.gui.core.others.UIRoot;
import me.brzeph.domain.gui.core.screens.*;
import me.brzeph.domain.gui.core.widgets.*;
import me.brzeph.domain.gui.impl.inventory.PlayerInventory;

import static me.brzeph.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY;
import static me.brzeph.constants.GUIConstants.PlayerConstants.PLAYER_INVENTORY_CLOSE_BTN_NAME;

public final class PlayerInventoryPlugin implements ScreenPlugin {

    @Override
    public String id() {
        return PLAYER_INVENTORY;
    }

    @Override
    public Screen build(ScreenContext ctx, ScreenParams params) {
        // -----------------  params -----------------
        float sw        = params.has("screenW")   ? params.get("screenW", Float.class) : 1280f;
        float sh        = params.has("screenH")   ? params.get("screenH", Float.class) : 720f;
        float weight    = params.has("weight")    ? params.get("weight", Float.class)  : 23f;
        float maxWeight = params.has("maxWeight") ? params.get("maxWeight", Float.class): 60f;
        Player player   = params.has("player")    ? params.get("player", Player.class) : null;

        // Player / Inventário (fonte de dados)
        assert player != null;
        PlayerInventory inv = player.getInventory();

        // ----------------- HEADER -----------------
        UIText title = new UIText().text("Inventário").font("title");
        UIButton btnFechar = new UIButton()
                .label("Fechar")
                .id(PLAYER_INVENTORY_CLOSE_BTN_NAME);

        Panel header = new Panel()
                .layout(new RowLayout(10f, 0f))
                .add(title.layout(LayoutParams.wrap().cross(Align.MIDDLE)))
                .add(new Panel().layout(new RowLayout(0f,0f)).layout(LayoutParams.flex(1f)))
                .add(btnFechar.layout(LayoutParams.wrap().cross(Align.MIDDLE)));

        // ----------------- INFO STRIP (card leve) -----------------
        UIMeter peso  = new UIMeter().value(weight / Math.max(1f, maxWeight))
                .height(12f)
                .label(String.format("Peso: %.00f / %.00f", weight, maxWeight));

        UIText goldTxt = new UIText().text("Ouro: " + inv.goldAmount()).font("default");

        Panel infoStrip = new Panel()
                .background(Color.rgba(0.12f,0.12f,0.14f,0.95f))
                .layout(new ColumnLayout(14f, 0f))
                .add(peso.layout(LayoutParams.flex(1f).cross(Align.START)))
                .add(new UISeparator().thickness(1f).color(Color.gray(0.20f,1f)).layout(LayoutParams.wrap()))
                .add(goldTxt.layout(LayoutParams.wrap().cross(Align.START)));

        // ----------------- EQUIPAMENTOS (card) -----------------
        Panel equipCard = new Panel()
                .background(Color.rgba(0.10f,0.10f,0.12f,0.92f))
                .layout(new ColumnLayout(10f, 10f))
                .add(new UIText().text("Equipamentos").font("default").layout(LayoutParams.wrap()))
                .add(new UISeparator().thickness(1f).color(Color.gray(0.18f,1f)).layout(LayoutParams.wrap()))
                .add(InventoryAdapter.makeEquipmentPanel(PLAYER_INVENTORY, inv, 8f, 10f)
                        .layout(LayoutParams.wrap()));

        // ----------------- MOCHILA (card com grid comum) -----------------
        UIGrid commonGrid = new UIGrid().layout(LayoutParams.flex(1f));
        InventoryAdapter.fillCommonGrid(commonGrid, inv, PLAYER_INVENTORY);

        Panel bagCard = new Panel()
                .background(Color.rgba(0.10f,0.10f,0.12f,0.92f))
                .layout(new ColumnLayout(10f, 10f))
                .add(new UIText().text("Mochila").font("default").layout(LayoutParams.wrap()))
                .add(new UISeparator().thickness(1f).color(Color.gray(0.18f,1f)).layout(LayoutParams.wrap()))
                .add(commonGrid) // FLEX ocupa o restante dentro do card
                .add(new UIText()
                        .text("Dica: arraste itens para equipar / mover. Shift = seleção múltipla.")
                        .font("default")
                        .layout(LayoutParams.wrap()));

        // ----------------- BODY (duas colunas) -----------------
        Panel body = new Panel()
                .layout(new RowLayout(14f, 0f))
                .add(equipCard.layout(LayoutParams.fixed(380f)))   // largura confortável p/ linhas de slots
                .add(bagCard.layout(LayoutParams.flex(1f)));

        // ----------------- JANELA -----------------
        Panel window = new Panel()
                .background(Color.rgba(0.08f,0.08f,0.09f,0.94f))
                .layout(new ColumnLayout(14f, 14f))
                .add(infoStrip.layout(LayoutParams.wrap()))
                .add(body.layout(LayoutParams.wrap()))
                .add(header.layout(LayoutParams.wrap()));

        UIRoot ui = new UIRoot(window);

        // centraliza janela
        float ww = Math.min(1120, sw * 0.92f);
        float wh = Math.min(720,  sh * 0.90f);
        float x  = (sw - ww) * 0.5f, y = (sh - wh) * 0.5f;
        ui.setBounds(x, y, ww, wh);

        return new Screen(id(), ui, ScreenLayer.PANEL)
                .setDisableFlyCamWhileActive(true)
                .setFreezeInputBehind(false)
                .setVisible(true)
                .setActive(true);
    }
}

