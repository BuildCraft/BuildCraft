/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.core.BCCoreSprites;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.button.GuiButtonDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

import java.util.function.Consumer;
import java.util.function.Supplier;

//public class GuiEmzuliPipe_BC8 extends GuiBC8<ContainerEmzuliPipe_BC8>
public class GuiEmzuliPipe_BC8 extends GuiBC8<ContainerEmzuliPipe_BC8> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("buildcrafttransport:textures/gui/pipe_emzuli.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_NO_PAINT = new GuiIcon(TEXTURE, SIZE_X, 40, 16, 16);
    private static final GuiButtonDrawable.Builder PAINT_BUTTON_BUILDER;

    static {
        GuiRectangle rect = new GuiRectangle(20, 20);
        GuiIcon enabled = new GuiIcon(TEXTURE, 176, 0, 20, 20, 256);
        PAINT_BUTTON_BUILDER = new GuiButtonDrawable.Builder(rect, enabled);
        PAINT_BUTTON_BUILDER.active = enabled.offset(0, 20);
    }

    public GuiEmzuliPipe_BC8(ContainerEmzuliPipe_BC8 container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        addButton(SlotIndex.SQUARE, 49, 19);
        addButton(SlotIndex.CIRCLE, 49, 47);
        addButton(SlotIndex.TRIANGLE, 106, 19);
        addButton(SlotIndex.CROSS, 106, 47);
    }

    private void addButton(SlotIndex index, int x, int y) {
        Supplier<DyeColor> getter = () -> container.behaviour.slotColours.get(index);
        Consumer<DyeColor> setter = c -> container.paintWidgets.get(index).setColour(c);

        IGuiPosition elem = mainGui.rootElement.offset(x, y);
        GuiButtonDrawable button = new GuiButtonDrawable(mainGui, index.name(), elem, PAINT_BUTTON_BUILDER);
        button.registerListener((b, key) ->
        {
            final DyeColor old = getter.get();
            DyeColor nColour;
            switch (key) {
                case 0: {
                    nColour = ColourUtil.getNextOrNull(old);
                    break;
                }
                case 1: {
                    nColour = ColourUtil.getPrevOrNull(old);
                    break;
                }
                case 2: {
                    nColour = null;
                    break;
                }
                default: {
                    return;
                }
            }
            setter.accept(nColour);
        });
        mainGui.shownElements.add(button);

        // Button paintbrush
        IGuiArea area = new GuiRectangle(20, 20).offset(elem);
        ISimpleDrawable paintIcon = (poseStack, px, py) ->
        {
            DyeColor colour = getter.get();
            if (colour == null) {
//                ICON_NO_PAINT.drawAt(px + 2, py + 2);
                ICON_NO_PAINT.drawAt(poseStack, px + 2, py + 2);
            } else {
                ISprite sprite = BCTransportSprites.ACTION_PIPE_COLOUR[colour.ordinal()];
                GuiIcon.drawAt(sprite, poseStack, px + 2, py + 2, 16);
            }
        };
        mainGui.shownElements.add(new GuiElementDrawable(mainGui, area, paintIcon, false));
        ITooltipElement tooltips = list ->
        {
            DyeColor colour = getter.get();
//            String line;
            Component line;
            if (colour == null) {
//                line = LocaleUtil.localize("gui.pipes.emzuli.nopaint");
                line = Component.translatable("gui.pipes.emzuli.nopaint");
            } else {
//                line = LocaleUtil.localize("gui.pipes.emzuli.paint", ColourUtil.getTextFullTooltip(colour));
                line = Component.translatable("gui.pipes.emzuli.paint", ColourUtil.getTextFullTooltip(colour));
            }
//            list.add(new ToolTip(line));
            list.add(new ToolTip(line));
        };
        mainGui.shownElements.add(new GuiElementToolTip(mainGui, area, tooltips));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, GuiGraphics guiGraphics) {
        ICON_GUI.drawAt(mainGui.rootElement, guiGraphics);

        SlotIndex currentSlot = container.behaviour.getCurrentSlot();
        for (SlotIndex index : container.behaviour.getActiveSlots()) {
            boolean current = index == currentSlot;
            int x = (index.ordinal() < 2 ? 4 : 155);
            int y = (index.ordinal() % 2 == 0 ? 21 : 49);
            ISprite sprite = current ? BCCoreSprites.TRIGGER_TRUE : BCLibSprites.LOCK;
            GuiIcon.drawAt(sprite, guiGraphics, mainGui.rootElement.getX() + x, mainGui.rootElement.getY() + y, 16);
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics guiGraphics) {
        String title = LocaleUtil.localize("gui.pipes.emzuli.title");
//        double titleX = mainGui.rootElement.getX() + (xSize - fontRenderer.getStringWidth(title)) / 2;
        double titleX = mainGui.rootElement.getX() + (imageWidth - font.width(title)) / 2;
//        fontRenderer.drawString(title, (int) titleX, (int) mainGui.rootElement.getY() + 6, 0x404040);
        guiGraphics.drawString(font, title, (int) titleX, (int) mainGui.rootElement.getY() + 6, 0x404040, false);

        int invX = (int) mainGui.rootElement.getX() + 8;
//        int invY = (int) mainGui.rootElement.getY() + ySize - 93;
        int invY = (int) mainGui.rootElement.getY() + imageHeight - 93;
//        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), invX, invY, 0x404040);
        guiGraphics.drawString(font, LocaleUtil.localize("gui.inventory"), invX, invY, 0x404040, false);
    }
}
