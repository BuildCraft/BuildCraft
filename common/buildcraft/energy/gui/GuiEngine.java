/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.SheetIcon;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngine;

public abstract class GuiEngine extends GuiBuildCraft {
	protected class EngineLedger extends Ledger {

		TileEngine engine;
		int headerColour = 0xe1c92f;
		int subheaderColour = 0xaaafb8;
		int textColour = 0x000000;

		public EngineLedger(TileEngine engine) {
			this.engine = engine;
			maxHeight = 94;
			overlayColor = 0xd46c1f;
		}

		@Override
		public void draw(int x, int y) {

			// Draw background
			drawBackground(x, y);

			// Draw icon
			bindTexture(ICONS_TEXTURE);
			drawIcon(new SheetIcon(ICONS_TEXTURE, 0, 0), x + 3, y + 4);

			if (!isFullyOpened()) {
				return;
			}

			fontRendererObj.drawString(StringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRendererObj.drawString(StringUtils.localize("gui.currentOutput") + ":", x + 22, y + 20, subheaderColour);
			fontRendererObj.drawString(String.format("%d RF/t", engine.currentOutput),
					x + 22, y + 32, textColour);
			fontRendererObj.drawString(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRendererObj.drawString(String.format("%d RF", engine.getEnergyStored()), x + 22,
					y + 56, textColour);
			fontRendererObj.drawString(StringUtils.localize("gui.heat") + ":", x + 22, y + 68, subheaderColour);
			fontRendererObj.drawString(String.format("%.2f \u00B0C", engine.getCurrentHeatValue()), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%d RF/t", engine.currentOutput);
		}
	}

	public GuiEngine(BuildCraftContainer container, IInventory inventory, ResourceLocation texture) {
		super(container, inventory, texture);
	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new EngineLedger((TileEngine) tile));
	}
}
