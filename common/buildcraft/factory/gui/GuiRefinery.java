/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory.gui;

import buildcraft.api.recipes.RefineryRecipes;
import buildcraft.api.recipes.RefineryRecipes.Recipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;
import buildcraft.factory.TileRefinery;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class GuiRefinery extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_filter.png");
	ContainerRefinery container;

	public GuiRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(new ContainerRefinery(inventory, refinery), refinery);

		xSize = 175;
		ySize = 207;

		this.container = (ContainerRefinery) this.inventorySlots;

		this.slots = new AdvancedSlot[3];

		this.slots[0] = new FluidSlot(38, 54);
		this.slots[1] = new FluidSlot(126, 54);
		this.slots[2] = new FluidSlot(82, 54);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.refineryBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString("->", 63, 59, 0x404040);
		fontRenderer.drawString("<-", 106, 59, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
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
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(mc.thePlayer.inventory.getItemStack());

			if (liquid == null)
				return;

			container.setFilter(position, liquid.getFluid());
		}
	}

	private void updateSlots() {

		Fluid filter0 = container.getFilter(0);
		Fluid filter1 = container.getFilter(1);

		((FluidSlot) slots[0]).stack = filter0;
		((FluidSlot) slots[1]).stack = filter1;

		FluidStack liquid0 = null;
		FluidStack liquid1 = null;

		if (filter0 != null) {
			liquid0 = new FluidStack(filter0, FluidContainerRegistry.BUCKET_VOLUME);
		}
		if (filter1 != null) {
			liquid1 = new FluidStack(filter1, FluidContainerRegistry.BUCKET_VOLUME);
		}

		Recipe recipe = RefineryRecipes.findRefineryRecipe(liquid0, liquid1);

		if (recipe != null) {
			((FluidSlot) slots[2]).stack = recipe.result.getFluid();
		} else {
			((FluidSlot) slots[2]).stack = null;
		}
	}

}
