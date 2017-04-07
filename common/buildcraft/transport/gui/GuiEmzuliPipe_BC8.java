package buildcraft.transport.gui;

import java.util.EnumMap;
import java.util.List;

import buildcraft.lib.gui.button.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.BCCoreSprites;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class GuiEmzuliPipe_BC8 extends GuiBC8<ContainerEmzuliPipe_BC8> implements IButtonClickEventListener {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/pipe_emzuli.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_BUTTON_UP = new GuiIcon(TEXTURE, SIZE_X, 0, 20, 20);
    private static final GuiIcon ICON_BUTTON_DOWN = new GuiIcon(TEXTURE, SIZE_X, 20, 20, 20);
    private static final GuiIcon ICON_NO_PAINT = new GuiIcon(TEXTURE, SIZE_X, 40, 16, 16);

    private final EnumMap<SlotIndex, GuiSpriteButton> colourButtons = new EnumMap<>(SlotIndex.class);

    public GuiEmzuliPipe_BC8(EntityPlayer player, PipeBehaviourEmzuli behaviour) {
        super(new ContainerEmzuliPipe_BC8(player, behaviour));
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        colourButtons.clear();
        addButton(SlotIndex.SQUARE, 49, 19);
        addButton(SlotIndex.CIRCLE, 49, 47);
        addButton(SlotIndex.TRIANGLE, 106, 19);
        addButton(SlotIndex.CROSS, 106, 47);
    }

    private void addButton(SlotIndex index, int x, int y) {
        GuiSpriteButton button = new GuiPaintButton(this, index.ordinal(), x + rootElement.getX(), y + rootElement.getY(), index);
        button.registerListener(this);
        colourButtons.put(index, button);
        guiElements.add(button);
    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger button, int buttonId, int buttonKey) {
        if (button instanceof GuiPaintButton) {
            GuiPaintButton paint = (GuiPaintButton) button;
            switch (buttonKey) {
                case 0: {
                    EnumDyeColor colour = paint.getCurrentColour();
                    colour = ColourUtil.getNextOrNull(colour);
                    container.paintWidgets.get(paint.index).setColour(colour);
                    break;
                }
                case 1: {
                    EnumDyeColor colour = paint.getCurrentColour();
                    colour = ColourUtil.getPrevOrNull(colour);
                    container.paintWidgets.get(paint.index).setColour(colour);
                    break;
                }
                case 2: {
                    container.paintWidgets.get(paint.index).setColour(null);
                }
            }
        }
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
        fontRenderer.drawString(title, rootElement.getX() + (xSize - fontRenderer.getStringWidth(title)) / 2, rootElement.getY() + 6, 0x404040);
        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), rootElement.getX() + 8, rootElement.getY() + ySize - 93, 0x404040);
    }

    public final class GuiPaintButton extends GuiSpriteButton {
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
                    tooltips.add(new ToolTip(String.format(LocaleUtil.localize("gui.pipes.emzuli.paint"), ColourUtil.getTextFullTooltip(color))));
                } else {
                    tooltips.add(new ToolTip(LocaleUtil.localize("gui.pipes.emzuli.nopaint")));
                }
            }
        }
    }
}
