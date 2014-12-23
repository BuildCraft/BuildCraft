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
import buildcraft.api.recipes.CraftingResult;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;
import buildcraft.silicon.TileAssemblyTable;

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

			// TODO (1.8): rewrite icon drawing
			/*Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
			drawIcon(BuildCraftCore.iconProvider.getIcon(CoreIconProvider.ENERGY), x + 3, y + 4);*/

			if (!isFullyOpened()) {
				return;
			}

			fontRendererObj.drawString(StringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRendererObj.drawString(StringUtils.localize("gui.assemblyCurrentRequired") + ":", x + 22, y + 20, subheaderColour);
			fontRendererObj.drawString(String.format("%d RF", table.clientRequiredEnergy), x + 22, y + 32, textColour);
			fontRendererObj.drawString(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRendererObj.drawString(String.format("%d RF", table.getEnergy()), x + 22, y + 56, textColour);
			fontRendererObj.drawString(StringUtils.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
			fontRendererObj.drawString(String.format("%.1f RF/t", table.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%.1f RF/t", table.getRecentEnergyAverage() / 100.0f);
		}
	}
	private final TileAssemblyTable table;

	class RecipeSlot extends AdvancedSlot {
		public CraftingResult<ItemStack> crafting;

		public RecipeSlot(int x, int y) {
			super(GuiAssemblyTable.this, x, y);
		}

		@Override
		public ItemStack getItemStack() {
			if (crafting != null) {
				return crafting.crafted;
			} else {
				return null;
			}
		}
	}

	public GuiAssemblyTable(IInventory playerInventory, TileAssemblyTable assemblyTable) {
		super(new ContainerAssemblyTable(playerInventory, assemblyTable), assemblyTable, TEXTURE);

		this.table = assemblyTable;
		xSize = 176;
		ySize = 207;

		for (int j = 0; j < 2; ++j) {
			for (int i = 0; i < 4; ++i) {
				slots.add(new RecipeSlot(134 + 18 * j, 36 + 18 * i));
			}
		}

		updateRecipes();
	}

	public void updateRecipes() {
		List<CraftingResult<ItemStack>> potentialRecipes = table.getPotentialOutputs();
		Iterator<CraftingResult<ItemStack>> cur = potentialRecipes.iterator();

		for (AdvancedSlot s : slots) {
			if (cur.hasNext()) {
				((RecipeSlot) s).crafting = cur.next();
			} else {
				((RecipeSlot) s).crafting = null;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.assemblyTableBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 15, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
		drawTooltipForSlotAt(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		updateRecipes();

		for (AdvancedSlot slot2 : slots) {
			RecipeSlot slot = (RecipeSlot) slot2;

			if (slot.crafting != null) {
				if (table.isAssembling(slot.crafting.recipe)) {
					drawTexturedModalRect(guiLeft + slot.x, guiTop + slot.y, 196, 1, 16, 16);
				} else if (table.isPlanned(slot.crafting.recipe)) {
					drawTexturedModalRect(guiLeft + slot.x, guiTop + slot.y, 177, 1, 16, 16);
				}
			}
		}

		int h = table.getProgressScaled(70);

		drawTexturedModalRect(guiLeft + 95, guiTop + 36 + 70 - h, 176, 18, 4, h);

		drawBackgroundSlots();
	}

	@Override
	protected void slotClicked(AdvancedSlot aslot, int mouseButton) {
		super.slotClicked(aslot, mouseButton);
		
		if (aslot instanceof RecipeSlot) {
			RecipeSlot slot = (RecipeSlot) aslot;
			
			if (slot.crafting == null) {
				return;
			}

			boolean select;

			if (table.isPlanned(slot.crafting.recipe)) {
				select = false;
			} else {
				select = true;
			}

			String id = slot.crafting.recipe.getId();

			table.rpcSelectRecipe(id, select);
		}
	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new LaserTableLedger());
	}
}
