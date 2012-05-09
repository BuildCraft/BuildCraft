/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.silicon;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.AssemblyRecipe;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.GuiAdvancedInterface;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketPayload;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable.SelectionMessage;

import org.lwjgl.opengl.GL11;

public class GuiAssemblyTable extends GuiAdvancedInterface {

	TileAssemblyTable assemblyTable;
	private IInventory playerInventory;

	class RecipeSlot extends AdvancedSlot {

		public AssemblyRecipe recipe;

		public RecipeSlot(int x, int y) {
			super(x, y);
		}

		@Override
		public ItemStack getItemStack() {
			if (this.recipe != null)
				return this.recipe.output;
			else
				return null;
		}
	}

	public GuiAssemblyTable(IInventory playerInventory,
			TileAssemblyTable assemblyTable) {
		super(new ContainerAssemblyTable(playerInventory, assemblyTable));

		this.playerInventory = playerInventory;
		this.assemblyTable = assemblyTable;
		xSize = 175;
		ySize = 207;

		slots = new AdvancedSlot[8];

		int p = 0;

		for (int j = 0; j < 2; ++j)
			for (int i = 0; i < 4; ++i) {
				slots[p] = new RecipeSlot(134 + 18 * j, 36 + 18 * i);
				p++;
			}

		updateRecipes();
	}

	public void updateRecipes() {
		LinkedList<AssemblyRecipe> potentialRecipes = assemblyTable
				.getPotentialOutputs();
		Iterator<AssemblyRecipe> cur = potentialRecipes.iterator();

		for (int p = 0; p < 8; ++p)
			if (cur.hasNext())
				((RecipeSlot) slots[p]).recipe = cur.next();
			else
				((RecipeSlot) slots[p]).recipe = null;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Assembly Table", 60, 15, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 97,
				0x404040);

		drawForegroundSelection();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/factory/gui/assembly_table.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		updateRecipes();

		for (int s = 0; s < slots.length; ++s) {
			RecipeSlot slot = (RecipeSlot) slots [s];

			if (assemblyTable.isAssembling(slot.recipe))
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 196,
						1, 16, 16);
			else if (assemblyTable.isPlanned(slot.recipe))
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 177,
						1, 16, 16);
		}

		int height = (int) assemblyTable.getCompletionRatio(70);

		drawTexturedModalRect(cornerX + 95, cornerY + 36 + 70 - height, 176,
				18, 4, height);

		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(i - cornerX, j - cornerY);

		if (position != -1) {
			RecipeSlot slot = (RecipeSlot) slots [position];

			SelectionMessage message = new SelectionMessage();

			if (assemblyTable.isPlanned(slot.recipe)) {
				assemblyTable.cancelPlanOutput(slot.recipe);
				message.select = false;
			} else {
				assemblyTable.planOutput(slot.recipe);
				message.select = true;
			}

			message.itemID = slot.recipe.output.itemID;
			message.itemDmg = slot.recipe.output.getItemDamage();

			ContainerAssemblyTable container = (ContainerAssemblyTable)inventorySlots;

			PacketPayload payload = TileAssemblyTable.selectionMessageWrapper
					.toPayload(container.x, container.y,
							container.z, message);

			PacketUpdate packet = new PacketUpdate(PacketIds.SELECTION_ASSEMBLY, payload);
			packet.posX = container.x;
			packet.posY = container.y;
			packet.posZ = container.z;
			CoreProxy.sendToServer(packet.getPacket());
		}

	}

	public void handleSelectionMessage(SelectionMessage message) {
		assemblyTable.handleSelectionMessage(message);

	}
}
