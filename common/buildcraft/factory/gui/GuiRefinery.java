/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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
import buildcraft.core.lib.gui.FluidSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.factory.TileRefinery;

public class GuiRefinery extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftfactory:textures/gui/refinery_filter.png");
	private final ContainerRefinery container;

	public GuiRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(new ContainerRefinery(inventory, refinery), null, TEXTURE);

		xSize = 175;
		ySize = 207;

		this.container = (ContainerRefinery) this.inventorySlots;

		this.slots.add(new FluidSlot(this, 38, 54));
		this.slots.add(new FluidSlot(this, 126, 54));
		this.slots.add(new FluidSlot(this, 82, 54));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.refineryBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString("->", 63, 59, 0x404040);
		fontRendererObj.drawString("<-", 106, 59, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);

		drawTooltipForSlotAt(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		updateSlots();
		drawBackgroundSlots(x, y);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int position = getSlotIndexAtLocation(i, j);

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
				if (position == 0) {
					container.setFilter(position, container.refinery.tanks[0].getFluidType());
				} else if (position == 1) {
					container.setFilter(position, container.refinery.tanks[1].getFluidType());
				}
			}
		}
	}

	private void updateSlots() {
		Fluid filter0 = container.getFilter(0);
		Fluid filter1 = container.getFilter(1);

		((FluidSlot) slots.get(0)).fluid = filter0;
		((FluidSlot) slots.get(0)).colorRenderCache = container.refinery.tanks[0].colorRenderCache;
		((FluidSlot) slots.get(1)).fluid = filter1;
		((FluidSlot) slots.get(1)).colorRenderCache = container.refinery.tanks[1].colorRenderCache;

		CraftingResult<FluidStack> crafting = container.refinery.craftingResult;

		if (crafting != null) {
			((FluidSlot) slots.get(2)).fluid = crafting.crafted.getFluid();
			((FluidSlot) slots.get(2)).colorRenderCache = crafting.crafted.getFluid().getColor(crafting.crafted);
		} else {
			((FluidSlot) slots.get(2)).fluid = null;
		}
	}
}
