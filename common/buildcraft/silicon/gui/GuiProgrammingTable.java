/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.silicon.TileProgrammingTable;

public class GuiProgrammingTable extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftsilicon:textures/gui/programming_table.png");

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
			fontRendererObj.drawString(String.format("%d RF", table.clientRequiredEnergy), x + 22, y + 32, textColour);
			fontRendererObj.drawStringWithShadow(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRendererObj.drawString(String.format("%d RF", table.getEnergy()), x + 22, y + 56, textColour);
			fontRendererObj.drawStringWithShadow(StringUtils.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
			fontRendererObj.drawString(String.format("%.1f RF/t", table.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%.1f RF/t", table.getRecentEnergyAverage() / 100.0f);
		}
	}

	private final TileProgrammingTable table;

	class RecipeSlot extends AdvancedSlot {
		public ItemStack slot;
		public int id;

		public RecipeSlot(int x, int y, int i) {
			super(GuiProgrammingTable.this, x, y);
			id = i;
		}

		@Override
		public ItemStack getItemStack() {
			return slot;
		}
	}

	public GuiProgrammingTable(IInventory playerInventory, TileProgrammingTable programmingTable) {
		super(new ContainerProgrammingTable(playerInventory, programmingTable), programmingTable, TEXTURE);

		this.table = programmingTable;
		xSize = 176;
		ySize = 207;

		for (int j = 0; j < TileProgrammingTable.HEIGHT; ++j) {
			for (int i = 0; i < TileProgrammingTable.WIDTH; ++i) {
				slots.add(new RecipeSlot(43 + 18 * i, 36 + 18 * j, (j * TileProgrammingTable.WIDTH) + i));
			}
		}

		updateRecipes();
	}

	public void updateRecipes() {
		if (table.options != null) {
			Iterator<ItemStack> cur = table.options.iterator();

			for (AdvancedSlot s : slots) {
				if (cur.hasNext()) {
					((RecipeSlot) s).slot = cur.next();
				} else {
					((RecipeSlot) s).slot = null;
				}
			}
		} else {
			for (AdvancedSlot s : slots) {
				((RecipeSlot) s).slot = null;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.programmingTableBlock.name");
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

		int i = 0;

		for (AdvancedSlot slot2 : slots) {
			RecipeSlot slot = (RecipeSlot) slot2;

			if (slot.slot != null) {
				if (table.optionId == i) {
					drawTexturedModalRect(guiLeft + slot.x, guiTop + slot.y, 196, 1, 16, 16);
				}
			}
			i++;
		}

		int h = table.getProgressScaled(70);

		drawTexturedModalRect(guiLeft + 164, guiTop + 36 + 70 - h, 176, 18, 4, h);

		drawBackgroundSlots(x, y);
	}

	@Override
	protected void slotClicked(AdvancedSlot aslot, int mouseButton) {
		super.slotClicked(aslot, mouseButton);

		if (aslot instanceof RecipeSlot) {
			RecipeSlot slot = (RecipeSlot) aslot;

			if (slot.slot == null) {
				return;
			}

			if (table.optionId == slot.id) {
				table.rpcSelectOption(-1);
			} else {
				table.rpcSelectOption(slot.id);
			}
		}
	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		if (!BuildCraftCore.hidePowerNumbers) {
			ledgerManager.add(new LaserTableLedger());
		}
	}
}
