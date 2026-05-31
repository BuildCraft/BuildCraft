/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.ScaledResolution;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreItems;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.tile.TileEngineRF;

public class GuiEngineRF extends GuiBC8<ContainerEngineRF> {
    private static final Identifier TEXTURE_BASE
        = new Identifier("buildcraftenergy:textures/gui/rf_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_RF = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 16, 60);
    private static final GuiIcon ICON_OVERLAY = new GuiIcon(TEXTURE_BASE, 57, 18, 80, 23);
    private static final GuiRectangle RECT_UPGRADES = new GuiRectangle(60, 42, 74, 20);
    private static final GuiRectangle RECT_UPGRADE_TYPES = new GuiRectangle(60, 20, 74, 20);
    private static final GuiRectangle RECT_RF_BATTERY = new GuiRectangle(30, 17, 8, 62);

    public GuiEngineRF(ContainerEngineRF container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
        mainGui.shownElements.add(
            new DummyHelpElement(
                RECT_UPGRADES.offset(mainGui.rootElement),
                new ElementHelpInfo(
                    "buildcraft.help.rf_engine.upgrades.title", 0xFF_FF_FF_FF, "buildcraft.help.rf_engine.upgrades.desc"
                )
            )
        );
        mainGui.shownElements.add(new GuiElementSimple(mainGui, RECT_UPGRADE_TYPES.offset(mainGui.rootElement)) {
            @Override
            public void addToolTips(List<ToolTip> tooltips) {
                if (contains(mainGui.mouse)) {
                    List<String> lines = new ArrayList<>();
                    lines.add(LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_types"));
                    for (Map.Entry<Item, Long> entry : TileEngineRF.RF_UPGRADE.entrySet()) {
                        String itemName = entry.getKey().getItemStackDisplayName(new ItemStack(entry.getKey()));
                        lines.add(itemName + " = +" + LocaleUtil.localizeMjFlow(entry.getValue()));
                    }
                    tooltips.add(new ToolTip(lines));
                }
            }
        });
        mainGui.shownElements.add(new GuiElementSimple(mainGui, RECT_RF_BATTERY.offset(mainGui.rootElement)) {
            @Override
            public void addHelpElements(List<HelpPosition> elements) {
                long mjFlow = container.tile.getMjPerTick();
                int rfFlow = (int) (mjFlow / BCLibConfig.mjRfConversion.mjPerRf);
                String rf = LocaleUtil.localizeRf(rfFlow);
                String mj = LocaleUtil.localizeMjFlow(mjFlow);
                String conversion = LocaleUtil.localize("buildcraft.help.rf_engine.rf_battery.desc", rf, mj);
                ElementHelpInfo help = ElementHelpInfo
                    .preTranslated("buildcraft.help.rf_engine.rf_battery.title", 0xFF_FF_FF_FF, conversion);
                elements.add(help.target(this));
            }

            @Override
            public void addToolTips(List<ToolTip> tooltips) {
                if (contains(mainGui.mouse)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(LocaleUtil.formatRf(container.tile.getCurrentRF()));
                    sb.append(" / ");
                    sb.append(LocaleUtil.localizeRf(TileEngineRF.MAX_RF));
                    tooltips.add(new ToolTip(sb.toString()));
                }
            }
        });
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);
        double rfHeight = 60.0 * container.tile.getCurrentRF() / TileEngineRF.MAX_RF;
        double scale = new ScaledResolution(mc).getScaleFactor();
        rfHeight = (Math.round(rfHeight * scale)) / scale;
        ICON_RF.drawCutInside(new GuiRectangle(31, 18 + 60 - rfHeight, 6, rfHeight).offset(mainGui.rootElement));

        int x = getGuiLeft();
        int y = getGuiTop();
        itemRender.renderItemAndEffectIntoGUI(new ItemStack(BCCoreItems.gearIron), x + 78, y + 22);
        itemRender.renderItemAndEffectIntoGUI(new ItemStack(BCCoreItems.gearGold), x + 101, y + 22);

        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 0.65f);
        ICON_OVERLAY.drawAt(mainGui.rootElement.offset(57, 18));
        RenderSystem.setShaderColor(1, 1, 1, 1f);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void drawForegroundLayer() {
        String str = LocaleUtil.localize("tile.engineRf.name");
        int strWidth = fontRenderer.getStringWidth(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2;
        double titleY = mainGui.rootElement.getY() + 6;
        fontRenderer.drawString(str, (int) titleX, (int) titleY, 0x404040);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
    }
}
