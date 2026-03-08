/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.container.ContainerChargingTable;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiChargingTable extends GuiBC8<ContainerChargingTable> {

    public static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/charging_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 132;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    // public GuiChargingTable(EntityPlayer player, TileChargingTable chargingTable)
    public GuiChargingTable(ContainerChargingTable container, PlayerInventory inventory, ITextComponent component) {
        // super(player, new ContainerChargingTable(player, chargingTable), chargingTable, TEXTURE);
        super(container, inventory, component);
        // xSize = 176;
        imageWidth = SIZE_X;
        // ySize = 132;
        imageHeight = SIZE_Y;

        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, MatrixStack poseStack) {
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);
    }

    @Override
    protected void drawForegroundLayer(MatrixStack poseStack) {
        String title = I18n.get(BCSiliconBlocks.chargingTable.get().getDescriptionId());
//        fontRenderer.drawString(title, guiLeft + (xSize - fontRenderer.getStringWidth(title)) / 2, guiTop + 15, 0x404040);
        font.draw(poseStack, title, leftPos + (float) (imageWidth - font.width(title)) / 2, topPos + 6, 0x404040);
    }
}
