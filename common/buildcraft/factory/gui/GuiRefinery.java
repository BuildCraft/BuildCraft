/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory.gui;

import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;

import org.lwjgl.opengl.GL11;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.recipes.RefineryRecipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtil;
import buildcraft.factory.TileRefinery;

public class GuiRefinery extends GuiAdvancedInterface {

	ContainerRefinery container;

	public GuiRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(new ContainerRefinery(inventory, refinery), refinery);

		xSize = 175;
		ySize = 207;

		this.container = (ContainerRefinery) this.inventorySlots;

		this.slots = new AdvancedSlot[3];

		this.slots[0] = new ItemSlot(38, 54);
		this.slots[1] = new ItemSlot(126, 54);
		this.slots[2] = new ItemSlot(82, 54);
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		String title = StringUtil.localize("tile.refineryBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString("->", 63, 59, 0x404040);
		fontRenderer.drawString("<-", 106, 59, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);

		drawForegroundSelection();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/refinery_filter.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		updateSlots();
		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(i - cornerX, j - cornerY);

		AdvancedSlot slot = null;

		if (position != -1 && position != 2)
			slot = slots[position];

		if (slot != null) {
			int liquidId = LiquidManager.getLiquidIDForFilledItem(mc.thePlayer.inventory.getItemStack());

			container.setFilter(position, liquidId, 0);
		}
	}

	private void updateSlots() {

		ItemStack filter0 = container.getFilter(0);
		ItemStack filter1 = container.getFilter(1);

		((ItemSlot) slots[0]).stack = filter0;
		((ItemSlot) slots[1]).stack = filter1;

		int liquid0Id = 0;
		int liquid1Id = 0;

		if (filter0 != null)
			liquid0Id = filter0.itemID;
		if (filter1 != null)
			liquid1Id = filter1.itemID;

		RefineryRecipe recipe = RefineryRecipe.findRefineryRecipe(new LiquidStack(liquid0Id, BuildCraftAPI.BUCKET_VOLUME, 0),
				new LiquidStack(liquid1Id, BuildCraftAPI.BUCKET_VOLUME, 0));

		if (recipe != null)
			((ItemSlot) slots[2]).stack = recipe.result.asItemStack();
		else
			((ItemSlot) slots[2]).stack = null;
	}

}
