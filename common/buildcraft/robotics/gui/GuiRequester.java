/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.robotics.TileRequester;

public class GuiRequester extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftrobotics:textures/gui/requester_gui.png");

	private TileRequester requester;

	private static class RequestSlot extends AdvancedSlot {

		private int index;

		public RequestSlot(GuiAdvancedInterface gui, int iIndex, int x, int y) {
			super(gui, x, y);

			index = iIndex;
		}

		public void setItem(ItemStack itemStack) {
			TileRequester requester = ((GuiRequester) gui).requester;

			requester.setRequest(index, itemStack);
			((GuiRequester) gui).getContainer().getRequestList();
		}

		@Override
		public ItemStack getItemStack() {
			ContainerRequester requester = ((GuiRequester) gui).getContainer();

			return requester.requests[index];
		}
	}

	public GuiRequester(IInventory iPlayerInventory, TileRequester iRequester) {
		super(new ContainerRequester(iPlayerInventory, iRequester), iPlayerInventory, TEXTURE);

		getContainer().gui = this;
		getContainer().getRequestList();

		xSize = 196;
		ySize = 181;

		requester = iRequester;

		for (int x = 0; x < 4; ++x) {
			for (int y = 0; y < 5; ++y) {
				slots.add(new RequestSlot(this, x * 5 + y, 9 + 18 * x, 7 + 18 * y));
			}
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		drawBackgroundSlots(x, y);
	}

	@Override
	protected void slotClicked(AdvancedSlot slot, int mouseButton) {
		super.slotClicked(slot, mouseButton);

		if (slot instanceof RequestSlot) {
			((RequestSlot) slot).setItem(mc.thePlayer.inventory.getItemStack());
		}
	}

	@Override
	public ContainerRequester getContainer() {
		return (ContainerRequester) super.getContainer();
	}
}