/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon.gui;

import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketNBT;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.StringUtils;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileAssemblyTable.SelectionMessage;
import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiAssemblyTable extends GuiAdvancedInterface {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft",DefaultProps.TEXTURE_PATH_GUI + "/assembly_table.png");
	TileAssemblyTable assemblyTable;

	class AssemblyLedger extends Ledger {
		int headerColour = 0xe1c92f;
		int subheaderColour = 0xaaafb8;
		int textColour = 0x000000;

		public AssemblyLedger() {
			maxHeight = 94;
			overlayColor = 0xd46c1f;
		}

		@Override
		public void draw(int x, int y) {

			// Draw background
			drawBackground(x, y);

			// Draw icon
            mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);
			drawIcon(BuildCraftCore.iconProvider.getIcon(CoreIconProvider.ENERGY), x + 3, y + 4);

			if (!isFullyOpened())
				return;

			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.assemblyCurrentRequired") + ":", x + 22, y + 20, subheaderColour);
			fontRenderer.drawString(String.format("%2.1f MJ", assemblyTable.getRequiredEnergy()), x + 22, y + 32, textColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRenderer.drawString(String.format("%2.1f MJ", assemblyTable.getStoredEnergy()), x + 22, y + 56, textColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
			fontRenderer.drawString(String.format("%3.2f MJ/t", assemblyTable.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%3.2f MJ/t", assemblyTable.getRecentEnergyAverage() / 100.0f);
		}

	}

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
		super(new ContainerAssemblyTable(playerInventory, assemblyTable), assemblyTable, TEXTURE);

		this.assemblyTable = assemblyTable;
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
		if (CoreProxy.proxy.isRenderWorld(assemblyTable.worldObj)) {
			CoreProxy.proxy.sendToServer(new PacketCoordinates(PacketIds.SELECTION_ASSEMBLY_GET, assemblyTable.xCoord, assemblyTable.yCoord,
					assemblyTable.zCoord).getPacket());
		}
	}

	public void updateRecipes() {
		LinkedList<AssemblyRecipe> potentialRecipes = assemblyTable.getPotentialOutputs();
		Iterator<AssemblyRecipe> cur = potentialRecipes.iterator();

		for (int p = 0; p < 8; ++p)
			if (cur.hasNext()) {
				((RecipeSlot) slots[p]).recipe = cur.next();
			} else {
				((RecipeSlot) slots[p]).recipe = null;
			}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.assemblyTableBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 15, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
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

		for (int s = 0; s < slots.length; ++s) {
			RecipeSlot slot = (RecipeSlot) slots[s];

			if (assemblyTable.isAssembling(slot.recipe)) {
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 196, 1, 16, 16);
			} else if (assemblyTable.isPlanned(slot.recipe)) {
				drawTexturedModalRect(cornerX + slot.x, cornerY + slot.y, 177, 1, 16, 16);
			}
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

			message.stack = slot.recipe.output;

			if (CoreProxy.proxy.isRenderWorld(assemblyTable.worldObj)) {

				PacketNBT packet = new PacketNBT(PacketIds.SELECTION_ASSEMBLY, message.getNBT(), assemblyTable.xCoord, assemblyTable.yCoord, assemblyTable.zCoord);

				CoreProxy.proxy.sendToServer(packet.getPacket());
			}
		}

	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new AssemblyLedger());
	}
}
