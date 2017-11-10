/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.transport.pipes.PipeFluidsEmerald;

public class GuiEmeraldFluidPipe extends GuiBuildCraft {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftcore:textures/gui/generic_one_slot.png");
	IInventory playerInventory;
	IInventory filterInventory;

	public GuiEmeraldFluidPipe(IInventory playerInventory, PipeFluidsEmerald pipe) {
		super(new ContainerEmeraldFluidPipe(playerInventory, pipe), pipe.getFilters(), TEXTURE);
		this.playerInventory = playerInventory;
		this.filterInventory = pipe.getFilters();
		xSize = 176;
		ySize = 132;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRendererObj.drawString(filterInventory.getInventoryName(), getCenteredOffset(filterInventory.getInventoryName()), 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
