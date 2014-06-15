/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.recipes.CraftingResult;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.FluidSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;
import buildcraft.factory.TileRefinery;

public class GuiRefinery extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_filter.png");
	ContainerRefinery container;

	public GuiRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(new ContainerRefinery(inventory, refinery), refinery, TEXTURE);

		xSize = 175;
		ySize = 207;

		this.container = (ContainerRefinery) this.inventorySlots;

		this.slots = new AdvancedSlot[3];

		this.slots[0] = new FluidSlot(this, 38, 54);
		this.slots[1] = new FluidSlot(this, 126, 54);
		this.slots[2] = new FluidSlot(this, 82, 54);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.refineryBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString("->", 63, 59, 0x404040);
		fontRendererObj.drawString("<-", 106, 59, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
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

		if (position >= 0 && position < 2) {
			if (k == 0) {
				if (!isShiftKeyDown()) {
					FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(mc.thePlayer.inventory.getItemStack());

					if (liquid == null) {
						return;
					}

					container.setFilter(position, liquid.getFluid());
					container.refinery.tankManager.get(position).colorRenderCache = liquid.getFluid().getColor(liquid);
				} else {
					container.setFilter(position, null);
					container.refinery.tankManager.get(position).colorRenderCache = 0xFFFFFF;
				}
			} else {
				TileRefinery ref = (TileRefinery) this.tile;

				if (position == 0) {
					container.setFilter(position, ref.tanks[0].getFluidType());
				} else if (position == 1) {
					container.setFilter(position, ref.tanks[1].getFluidType());
				}
			}
		}
	}

	private void updateSlots() {
		Fluid filter0 = container.getFilter(0);
		Fluid filter1 = container.getFilter(1);

		((FluidSlot) slots[0]).fluid = filter0;
		((FluidSlot) slots[0]).colorRenderCache = container.refinery.tanks[0].colorRenderCache;
		((FluidSlot) slots[1]).fluid = filter1;
		((FluidSlot) slots[1]).colorRenderCache = container.refinery.tanks[1].colorRenderCache;

		CraftingResult<FluidStack> crafting = container.refinery.craftingResult;

		if (crafting != null) {
			((FluidSlot) slots[2]).fluid = crafting.crafted.getFluid();
			((FluidSlot) slots[2]).colorRenderCache = crafting.crafted.getFluid().getColor(crafting.crafted);
		} else {
			((FluidSlot) slots[2]).fluid = null;
		}
	}
}
