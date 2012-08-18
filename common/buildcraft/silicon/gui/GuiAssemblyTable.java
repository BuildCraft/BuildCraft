/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon.gui;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

import org.lwjgl.opengl.GL11;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.ProxyCore;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.utils.StringUtil;
import buildcraft.factory.TileAssemblyTable;
import buildcraft.factory.TileAssemblyTable.SelectionMessage;

public class GuiAssemblyTable extends GuiAdvancedInterface {

	TileAssemblyTable assemblyTable;

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

	public GuiAssemblyTable(IInventory playerInventory, TileAssemblyTable assemblyTable) {
		super(new ContainerAssemblyTable(playerInventory, assemblyTable), assemblyTable);

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

		// Request current selection from server
		if(ProxyCore.proxy.isRemote(assemblyTable.worldObj))
			ProxyCore.proxy.sendToServer(new PacketCoordinates(PacketIds.SELECTION_ASSEMBLY_GET, assemblyTable.xCoord,
				assemblyTable.yCoord, assemblyTable.zCoord).getPacket());
	}

	public void updateRecipes() {
		LinkedList<AssemblyRecipe> potentialRecipes = assemblyTable.getPotentialOutputs();
		Iterator<AssemblyRecipe> cur = potentialRecipes.iterator();

		for (int p = 0; p < 8; ++p)
			if (cur.hasNext())
				((RecipeSlot) slots[p]).recipe = cur.next();
			else
				((RecipeSlot) slots[p]).recipe = null;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		String title = StringUtil.localize("tile.assemblyTableBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 15, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, ySize - 97, 0x404040);

		drawForegroundSelection();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/assembly_table.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		updateRecipes();

		for (int s = 0; s < slots.length; ++s) {
			RecipeSlot slot = (RecipeSlot) slots[s];

			if (assemblyTable.isAssembling(slot.recipe))
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 196, 1, 16, 16);
			else if (assemblyTable.isPlanned(slot.recipe))
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 177, 1, 16, 16);
		}

		int height = (int) assemblyTable.getCompletionRatio(70);

		drawTexturedModalRect(cornerX + 95, cornerY + 36 + 70 - height, 176, 18, 4, height);

		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(i - cornerX, j - cornerY);

		if (position != -1) {
			RecipeSlot slot = (RecipeSlot) slots[position];
			
			if (slot.recipe == null)
				return;

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

			ContainerAssemblyTable container = (ContainerAssemblyTable) inventorySlots;

			if (ProxyCore.proxy.isRemote(assemblyTable.worldObj)) {
				PacketPayload payload = TileAssemblyTable.selectionMessageWrapper.toPayload(container.x, container.y,
						container.z, message);

				PacketUpdate packet = new PacketUpdate(PacketIds.SELECTION_ASSEMBLY, payload);
				packet.posX = assemblyTable.xCoord;
				packet.posY = assemblyTable.yCoord;
				packet.posZ = assemblyTable.zCoord;

				ProxyCore.proxy.sendToServer(packet.getPacket());
			}
		}

	}

	public void handleSelectionMessage(SelectionMessage message) {
		assemblyTable.handleSelectionMessage(message);

	}
}
