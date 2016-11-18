/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;
import buildcraft.core.lib.gui.widgets.ButtonWidget;
import buildcraft.core.lib.network.PacketGuiReturn;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.pipes.PipeItemsEmzuli;

public class ContainerEmzuliPipe extends BuildCraftContainer {

    private final PipeItemsEmzuli pipe;
    private final IInventory filterInv;
    private final byte[] prevSlotColors = new byte[4];
    private final PaintWidget[] paintWidgets = new PaintWidget[4];

    public ContainerEmzuliPipe(EntityPlayer player, PipeItemsEmzuli pipe) {
        super(player, pipe.getFilters().getSizeInventory());

        this.pipe = pipe;
        filterInv = pipe.getFilters();

        addWidget(paintWidgets[0] = new PaintWidget(0, 51, 19));
        addWidget(paintWidgets[1] = new PaintWidget(1, 104, 19));
        addWidget(paintWidgets[2] = new PaintWidget(2, 51, 47));
        addWidget(paintWidgets[3] = new PaintWidget(3, 104, 47));

        addSlot(new SlotPhantom(filterInv, 0, 25, 21));
        addSlot(new SlotPhantom(filterInv, 1, 134, 21));
        addSlot(new SlotPhantom(filterInv, 2, 25, 49));
        addSlot(new SlotPhantom(filterInv, 3, 134, 49));

        for (int l = 0; l < 3; l++) {
            for (int k1 = 0; k1 < 9; k1++) {
                addSlotToContainer(new Slot(player.inventory, k1 + l * 9 + 9, 8 + k1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 142));
        }
    }

    @Override
    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);
        for (int slot = 0; slot < pipe.slotColors.length; slot++) {
            prevSlotColors[slot] = pipe.slotColors[slot];
            player.sendProgressBarUpdate(this, slot, pipe.slotColors[slot]);
        }
    }

    /** Updates crafting matrix; called from onCraftMatrixChanged. Args: none */
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object crafter : crafters) {
            ICrafting player = (ICrafting) crafter;

            for (int slot = 0; slot < pipe.slotColors.length; slot++) {
                if (prevSlotColors[slot] != pipe.slotColors[slot]) {
                    player.sendProgressBarUpdate(this, slot, pipe.slotColors[slot]);
                }
            }
        }
        System.arraycopy(pipe.slotColors, 0, prevSlotColors, 0, pipe.slotColors.length);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        paintWidgets[id].currentColor = data;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return pipe.container.isUseableByPlayer(entityplayer);
    }

    private class PaintWidget extends ButtonWidget {

        private final int slot;
        private int currentColor = 0;

        private ToolTip toolTip = new ToolTip(500) {
            @Override
            public void refresh() {
                toolTip.clear();
                EnumDyeColor color = getCurrent();
                if (color != null) {
                    toolTip.add(new ToolTipLine(String.format(StringUtilBC.localize("gui.pipes.emzuli.paint"), color.getLocalizedName())));
                } else {
                    toolTip.add(new ToolTipLine(StringUtilBC.localize("gui.pipes.emzuli.nopaint")));
                }
            }
        };

        public PaintWidget(int slot, int x, int y) {
            super(x, y, 176, 0, 20, 20);
            this.slot = slot;
        }

        private EnumDyeColor getCurrent() {
            return currentColor == 0 ? null : EnumDyeColor.byMetadata(currentColor - 1);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
            super.draw(gui, guiX, guiY, mouseX, mouseY);
            EnumDyeColor color = getCurrent();
            if (color != null) {
                GuiBuildCraft.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                gui.drawTexturedModalRect(guiX + x + 2, guiY + y + 2, BuildCraftTransport.actionPipeColor[color.ordinal()].getGuiSprite(), 16, 16);
            } else {
                gui.drawTexturedModalRect(guiX + x + 2, guiY + y + 2, u, v + h + h, 16, 16);
            }
        }

        @Override
        public void onRelease(int mouseButton) {
            switch (mouseButton) {
                case 0:
                    currentColor++;
                    if (currentColor > 16) {
                        currentColor = 0;
                    }
                    break;
                case 1:
                    currentColor--;
                    if (currentColor < 0) {
                        currentColor = 16;
                    }
                    break;
                case 2:
                    currentColor = 0;
                    break;
            }
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                DataOutputStream data = new DataOutputStream(bytes);
                data.writeByte(slot);
                EnumDyeColor color = getCurrent();
                data.writeByte(color == null ? 0 : color.getMetadata() + 1);
                PacketGuiReturn pkt = new PacketGuiReturn(pipe.getContainer(), bytes.toByteArray());
                pkt.sendPacket();
            } catch (IOException ex) {}
        }

        @Override
        public ToolTip getToolTip() {
            return toolTip;
        }
    }
}
