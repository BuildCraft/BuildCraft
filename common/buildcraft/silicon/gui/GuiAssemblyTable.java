/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketNBT;
import buildcraft.core.recipes.AssemblyRecipeManager.AssemblyRecipe;
import buildcraft.core.utils.StringUtils;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileAssemblyTable.SelectionMessage;

public class GuiAssemblyTable extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/assembly_table.png");

	private class LaserTableLedger extends Ledger {

		int headerColour = 0xe1c92f;
		int subheaderColour = 0xaaafb8;
		int textColour = 0x000000;

		public LaserTableLedger() {
			maxHeight = 94;
			overlayColor = 0xd46c1f;
		}

		@Override
		public void draw(int x, int y) {

			// Draw background
			drawBackground(x, y);

			// Draw icon
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
			drawIcon(BuildCraftCore.iconProvider.getIcon(CoreIconProvider.ENERGY), x + 3, y + 4);

			if (!isFullyOpened()) {
				return;
			}

			fontRendererObj.drawStringWithShadow(StringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRendererObj.drawStringWithShadow(StringUtils.localize("gui.assemblyCurrentRequired") + ":", x + 22, y + 20, subheaderColour);
			fontRendererObj.drawString(String.format("%2.1f MJ", table.clientRequiredEnergy), x + 22, y + 32, textColour);
			fontRendererObj.drawStringWithShadow(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRendererObj.drawString(String.format("%2.1f MJ", table.getEnergy()), x + 22, y + 56, textColour);
			fontRendererObj.drawStringWithShadow(StringUtils.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
			fontRendererObj.drawString(String.format("%3.2f MJ/t", table.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%3.2f MJ/t", table.getRecentEnergyAverage() / 100.0f);
		}
	}
	private final TileAssemblyTable table;

	class RecipeSlot extends AdvancedSlot {

		public AssemblyRecipe recipe;

		public RecipeSlot(int x, int y) {
			super(GuiAssemblyTable.this, x, y);
		}

		@Override
		public ItemStack getItemStack() {
			if (this.recipe != null) {
				return this.recipe.getOutput();
			} else {
				return null;
			}
		}
	}

	public GuiAssemblyTable(IInventory playerInventory, TileAssemblyTable assemblyTable) {
		super(new ContainerAssemblyTable(playerInventory, assemblyTable), assemblyTable, TEXTURE);

		this.table = assemblyTable;
		xSize = 175;
		ySize = 207;

		slots = new AdvancedSlot[8];

		int p = 0;

		for (int j = 0; j < 2; ++j) {
			for (int i = 0; i < 4; ++i) {
				slots[p] = new RecipeSlot(134 + 18 * j, 36 + 18 * i);
				p++;
			}
		}

		updateRecipes();

		// Request current selection from server
		if (assemblyTable.getWorldObj().isRemote) {
			BuildCraftSilicon.instance.sendToServer(new PacketCoordinates(PacketIds.SELECTION_ASSEMBLY_GET, assemblyTable.xCoord, assemblyTable.yCoord,
					assemblyTable.zCoord));
		}
	}

	public void updateRecipes() {
		List<AssemblyRecipe> potentialRecipes = table.getPotentialOutputs();
		Iterator<AssemblyRecipe> cur = potentialRecipes.iterator();

		for (int p = 0; p < 8; ++p) {
			if (cur.hasNext()) {
				((RecipeSlot) slots[p]).recipe = cur.next();
			} else {
				((RecipeSlot) slots[p]).recipe = null;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.assemblyTableBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 15, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		updateRecipes();

		for (AdvancedSlot slot2 : slots) {
			RecipeSlot slot = (RecipeSlot) slot2;

			if (table.isAssembling(slot.recipe)) {
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 196, 1, 16, 16);
			} else if (table.isPlanned(slot.recipe)) {
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 177, 1, 16, 16);
			}
		}

		int h = table.getProgressScaled(70);

		drawTexturedModalRect(cornerX + 95, cornerY + 36 + 70 - h, 176, 18, 4, h);

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

			if (slot.recipe == null) {
				return;
			}

			SelectionMessage message = new SelectionMessage();

			if (table.isPlanned(slot.recipe)) {
				table.cancelPlanOutput(slot.recipe);
				message.select = false;
			} else {
				table.planOutput(slot.recipe);
				message.select = true;
			}

			message.stack = slot.recipe.output;

			if (table.getWorldObj().isRemote) {
				PacketNBT packet = new PacketNBT(PacketIds.SELECTION_ASSEMBLY, message.getNBT(), table.xCoord, table.yCoord, table.zCoord);
				BuildCraftSilicon.instance.sendToServer(packet);
			}
		}
	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new LaserTableLedger());
	}
}
