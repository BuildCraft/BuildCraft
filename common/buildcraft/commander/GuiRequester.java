/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;

public class GuiRequester extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_GUI + "/requester_gui.png");

	private TileRequester requester;

	private IInventory playerInventory;

	private static class RequestSlot extends AdvancedSlot {

		private ItemStack item;

		public RequestSlot(GuiAdvancedInterface gui, int x, int y) {
			super(gui, x, y);
		}

		public void setItem(ItemStack itemStack) {
			item = itemStack.copy();
		}

		@Override
		public ItemStack getItemStack() {
			return item;
		}
	}

	public GuiRequester(IInventory iPlayerInventory, TileRequester iRequester) {
		super(new ContainerRequester(iPlayerInventory, iRequester), iPlayerInventory, TEXTURE);

		getContainer().gui = this;

		xSize = 256;
		ySize = 220;

		requester = iRequester;
		playerInventory = iPlayerInventory;

		for (int x = 0; x < 4; ++x) {
			for (int y = 0; y < 5; ++y) {

				slots.add(new RequestSlot(this, 9 + 18 * x, 7 + 18 * y));
			}
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		RequestSlot slot = (RequestSlot) getSlotAtLocation(mouseX, mouseY);

		if (slot != null) {
			slot.setItem(mc.thePlayer.inventory.getItemStack());
		}
	}

	@Override
	public ContainerRequester getContainer() {
		return (ContainerRequester) super.getContainer();
	}
}