/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.button.GuiButtonDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class GuiEmzuliPipe_BC8 extends GuiBC8<ContainerEmzuliPipe_BC8> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation("buildcrafttransport:textures/gui/pipe_emzuli.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_NO_PAINT = new GuiIcon(TEXTURE, SIZE_X, 40, 16, 16);
    private static final GuiButtonDrawable.Builder PAINT_BUTTON_BUILDER;

    static {
        GuiRectangle rect = new GuiRectangle(20, 20);
        GuiIcon enabled = new GuiIcon(TEXTURE, 175, 0, 20, 20, 256);
        PAINT_BUTTON_BUILDER = new GuiButtonDrawable.Builder(rect, enabled);
        PAINT_BUTTON_BUILDER.active = enabled.offset(0, 20);
    }

    public GuiEmzuliPipe_BC8(EntityPlayer player, PipeBehaviourEmzuli behaviour) {
        super(new ContainerEmzuliPipe_BC8(player, behaviour));
        xSize = SIZE_X;
        ySize = SIZE_Y;
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
        Supplier<EnumDyeColor> getter = () -> null;
        Consumer<EnumDyeColor> setter = c -> container.paintWidgets.get(index).setColour(c);

        IGuiPosition elem = rootElement.offset(x, y);
        GuiButtonDrawable button = new GuiButtonDrawable(this, index.name(), elem, PAINT_BUTTON_BUILDER);
        button.registerListener((b, key) -> {
            switch (key) {
                case 0: {
                    EnumDyeColor colour = getter.get();
                    setter.accept(ColourUtil.getNextOrNull(colour));
                    break;
                }
                case 1: {
                    EnumDyeColor colour = getter.get();
                    setter.accept(ColourUtil.getPrevOrNull(colour));
                    break;
                }
                case 2: {
                    setter.accept(null);
                }
            }
        });
        guiElements.add(button);

        // Button paintbrush
        IGuiArea area = new GuiRectangle(20, 20).offset(elem);
        ISimpleDrawable paintIcon = (px, py) -> {
            EnumDyeColor colour = container.behaviour.slotColours.get(index);
            if (colour == null) {
                ICON_NO_PAINT.drawAt(px + 2, py + 2);
            } else {
                ISprite sprite = BCTransportSprites.ACTION_PIPE_COLOUR[colour.ordinal()];
                GuiIcon.drawAt(sprite, px, py, 16);
            }
        };
        guiElements.add(new GuiElementDrawable(this, area, paintIcon, false));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        SlotIndex currentSlot = container.behaviour.getCurrentSlot();
        for (SlotIndex index : container.behaviour.getActiveSlots()) {
            boolean current = index == currentSlot;
            int x = (index.ordinal() < 2 ? 4 : 155);
            int y = (index.ordinal() % 2 == 0 ? 21 : 49);
            ISprite sprite = current ? BCCoreSprites.TRIGGER_TRUE : BCLibSprites.LOCK;
            GuiIcon.drawAt(sprite, rootElement.getX() + x, rootElement.getY() + y, 16);
        }
    }

    @Override
    protected void drawForegroundLayer() {
        String title = LocaleUtil.localize("gui.pipes.emzuli.title");
        fontRenderer.drawString(title, rootElement.getX() + (xSize - fontRenderer.getStringWidth(title)) / 2,
            rootElement.getY() + 6, 0x404040);
        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), rootElement.getX() + 8,
            rootElement.getY() + ySize - 93, 0x404040);
    }
/*
    public final class GuiPaintButton extends GuiButtonDrawable {
        private final SlotIndex index;

        public GuiPaintButton(GuiBC8<?> gui, int buttonId, int x, int y, SlotIndex index) {
            super(gui, buttonId, x, y, ICON_BUTTON_UP, ICON_BUTTON_DOWN);
            this.index = index;
            this.width = 20;
            this.height = 20;
            setBehaviour(IButtonBehaviour.DEFAULT);
        }

        private EnumDyeColor getCurrentColour() {
            return container.behaviour.slotColours.get(index);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            super.drawButton(mc, mouseX, mouseY);
            EnumDyeColor colour = getCurrentColour();
            if (colour == null) {
                ICON_NO_PAINT.drawAt(getX() + 2, getY() + 2);
            } else {
                GuiIcon.drawAt(BCTransportSprites.ACTION_PIPE_COLOUR[colour.ordinal()], getX() + 2, getY() + 2, 16);
            }
        }

        @Override
        public void addToolTips(List<ToolTip> tooltips) {
            if (contains(gui.mouse)) {
                EnumDyeColor color = getCurrentColour();
                if (color != null) {
                    tooltips.add(new ToolTip(
                        LocaleUtil.localize("gui.pipes.emzuli.paint", ColourUtil.getTextFullTooltip(color))));
                } else {
                    tooltips.add(new ToolTip(LocaleUtil.localize("gui.pipes.emzuli.nopaint")));
                }
            }
        }
    }
*/
}
