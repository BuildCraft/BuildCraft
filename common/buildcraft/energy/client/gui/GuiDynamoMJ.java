/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.core.BCCoreItems;
import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiDynamoMJ extends GuiBC8<ContainerDynamoMJ> {
    private static final ResourceLocation TEXTURE_BASE
            = new ResourceLocation("buildcraftenergy:textures/gui/mj_dynamo_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_RF = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 16, 60);
    private static final GuiIcon ICON_OVERLAY = new GuiIcon(TEXTURE_BASE, 39, 18, 80, 23);
    private static final GuiRectangle RECT_UPGRADES = new GuiRectangle(42, 42, 74, 20);
    private static final GuiRectangle RECT_UPGRADE_TYPES = new GuiRectangle(42, 20, 74, 20);
    private static final GuiRectangle RECT_RF_BATTERY = new GuiRectangle(138, 17, 8, 62);

    public GuiDynamoMJ(ContainerDynamoMJ container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerDynamoMJ(mainGui, container.tile, true));
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
                    // List<String> lines = new ArrayList<>();
                    List<Component> lines = new ArrayList<>();
                    // lines.add(LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_types"));
                    lines.add(new TranslatableComponent("buildcraft.gui.rf_engine.upgrade_types"));
                    // for (Map.Entry<Item, Long> entry : TileEngineRF.RF_UPGRADE.entrySet())
                    for (Map.Entry<RegistryObject<ItemBC_Neptune>, Long> entry : TileEngineRF.RF_UPGRADE.entrySet()) {
                        // String itemName = entry.getKey().getItemStackDisplayName(new ItemStack(entry.getKey()));
                        Component itemName = entry.getKey().get().getName(new ItemStack(entry.getKey().get()));
                        long mj = entry.getValue();
                        int rf = (int) (mj / BCLibConfig.mjRfConversion.mjPerRf);
                        // lines.add(itemName + " = +" + LocaleUtil.localizeRfFlow(rf));
                        lines.add(new TextComponent("").append(itemName).append(" = +").append(LocaleUtil.localizeRfFlowComponent(rf)));
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
                String mj = LocaleUtil.localizeMj(mjFlow);
                String rf = LocaleUtil.localizeRfFlow(rfFlow);
                String conversion = LocaleUtil.localize("buildcraft.help.mj_dynamo.rf_battery.desc", mj, rf);
                ElementHelpInfo help = ElementHelpInfo
                        .preTranslated("buildcraft.help.mj_dynamo.rf_battery.title", 0xFF_FF_FF_FF, conversion);
                elements.add(help.target(this));
            }

            @Override
            public void addToolTips(List<ToolTip> tooltips) {
                if (contains(mainGui.mouse)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(LocaleUtil.formatRf(container.tile.getCurrentRF()));
                    sb.append(" / ");
                    sb.append(LocaleUtil.localizeRf(TileEngineRF.MAX_RF));
                    // tooltips.add(new ToolTip(sb.toString()));
                    tooltips.add(new ToolTip(new TextComponent(sb.toString())));
                }
            }
        });
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);
        double rfHeight = 60.0 * container.tile.getCurrentRF() / TileEngineRF.MAX_RF;
        // double scale = new ScaledResolution(mc).getScaleFactor();
        double scale = minecraft.getWindow().getGuiScale();
        rfHeight = (Math.round(rfHeight * scale)) / scale;
        ICON_RF.drawCutInside(new GuiRectangle(139, 18 + 60 - rfHeight, 6, rfHeight).offset(mainGui.rootElement), poseStack);

        int x = getGuiLeft();
        int y = getGuiTop();
        // itemRender.renderItemAndEffectIntoGUI(new ItemStack(BCCoreItems.gearIron), x + 60, y + 22);
        itemRenderer.renderAndDecorateItem(new ItemStack(BCCoreItems.gearIron.get()), x + 60, y + 22);
        // itemRender.renderItemAndEffectIntoGUI(new ItemStack(BCCoreItems.gearGold), x + 83, y + 22);
        itemRenderer.renderAndDecorateItem(new ItemStack(BCCoreItems.gearGold.get()), x + 83, y + 22);

        // GlStateManager.disableDepth();
        RenderUtil.disableDepth();
        // GlStateManager.color(1, 1, 1, 0.65f);
        RenderUtil.color(1, 1, 1, 0.65f);
        ICON_OVERLAY.drawAt(mainGui.rootElement.offset(39, 18), poseStack);
        // GlStateManager.color(1, 1, 1, 1f);
        RenderUtil.color(1, 1, 1, 1f);
        // GlStateManager.enableDepth();
        RenderUtil.enableDepth();
    }

    @Override
    protected void drawForegroundLayer(PoseStack poseStack) {
        String str = LocaleUtil.localize("tile.mjDynamo.name");
        int strWidth = font.width(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2;
        double titleY = mainGui.rootElement.getY() + 6;
        // fontRenderer.drawString(str, (int) titleX, (int) titleY, 0x404040);
        font.draw(poseStack, str, (int) titleX, (int) titleY, 0x404040);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        // fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
        font.draw(poseStack, LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
    }
}
