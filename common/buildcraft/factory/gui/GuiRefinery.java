/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

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
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtil.localize("tile.refineryBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString("->", 63, 59, 0x404040);
		fontRenderer.drawString("<-", 106, 59, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);

		drawForegroundSelection(par1, par2);
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

		if (position != -1 && position != 2) {
			slot = slots[position];
		}

		if (slot != null) {
			LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(mc.thePlayer.inventory.getItemStack());

			if (liquid == null)
				return;

			container.setFilter(position, liquid.itemID, liquid.itemMeta);
		}
	}

	private void updateSlots() {

		ItemStack filter0 = container.getFilter(0);
		ItemStack filter1 = container.getFilter(1);

		((ItemSlot) slots[0]).stack = filter0;
		((ItemSlot) slots[1]).stack = filter1;

		LiquidStack liquid0 = null;
		LiquidStack liquid1 = null;

		if (filter0 != null) {
			liquid0 = new LiquidStack(filter0.itemID, LiquidContainerRegistry.BUCKET_VOLUME, filter0.getItemDamage());
		}
		if (filter1 != null) {
			liquid1 = new LiquidStack(filter1.itemID, LiquidContainerRegistry.BUCKET_VOLUME, filter1.getItemDamage());
		}

		RefineryRecipe recipe = RefineryRecipe.findRefineryRecipe(liquid0, liquid1);

		if (recipe != null) {
			((ItemSlot) slots[2]).stack = recipe.result.asItemStack();
		} else {
			((ItemSlot) slots[2]).stack = null;
		}
	}

}
