/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.list;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import buildcraft.BuildCraftCore;
import buildcraft.core.ItemList;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;

public class GuiListNew extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE_BASE = new ResourceLocation(
			"buildcraftcore:textures/gui/list_new.png");

	private GuiTextField textField;
	private EntityPlayer player;

	private static class ListSlot extends AdvancedSlot {
		public int lineIndex;
		public int slotIndex;

		public ListSlot(GuiAdvancedInterface gui, int x, int y, int iLineIndex, int iSlotIndex) {
			super(gui, x, y);

			lineIndex = iLineIndex;
			slotIndex = iSlotIndex;
		}

		@Override
		public ItemStack getItemStack() {
			ContainerListNew container = (ContainerListNew) gui.getContainer();
			return container.lines[lineIndex].getStack(slotIndex);
		}
	}

	public GuiListNew(EntityPlayer iPlayer) {
		super(new ContainerListNew(iPlayer), iPlayer.inventory, TEXTURE_BASE);

		xSize = 176;
		ySize = 206;

		for (int sy = 0; sy < ListHandlerNew.HEIGHT; sy++) {
			for (int sx = 0; sx < ListHandlerNew.WIDTH; sx++) {
				slots.add(new ListSlot(this, 8 + sx * 18, 46 + sy * 33, sy, sx));
			}
		}

		player = iPlayer;
	}

	@Override
	public void initGui() {
		super.initGui();

		textField = new GuiTextField(this.fontRendererObj, 10, 10, 156, 12);
		textField.setMaxStringLength(32);
		textField.setText(BuildCraftCore.listItem.getLabel(player.getCurrentEquippedItem()));
		textField.setFocused(false);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		ContainerListNew container = (ContainerListNew) getContainer();
		drawBackgroundSlots(x, y);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		textField.drawTextBox();
		drawTooltipForSlotAt(par1, par2);
	}

	private boolean isCarryingList() {
		ItemStack stack = mc.thePlayer.inventory.getItemStack();
		return stack != null && stack.getItem() instanceof ItemList;
	}

	private boolean hasListEquipped() {
		return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemList;
	}

	@Override
	protected void mouseClicked(int x, int y, int b) {
		super.mouseClicked(x, y, b);

		if (isCarryingList() || !hasListEquipped()) {
			return;
		}

		AdvancedSlot slot = getSlotAtLocation(x, y);
		ContainerListNew container = (ContainerListNew) getContainer();

		if (slot instanceof ListSlot) {
			container.setStack(((ListSlot) slot).lineIndex, ((ListSlot) slot).slotIndex, mc.thePlayer.inventory.getItemStack());
		}

		textField.mouseClicked(x - guiLeft, y - guiTop, b);
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (textField.isFocused()) {
			if (c == 13 || c == 27) {
				textField.setFocused(false);
			} else {
				textField.textboxKeyTyped(c, i);
				((ContainerListNew) container).setLabel(textField.getText());
			}
		} else {
			super.keyTyped(c, i);
		}
	}
}