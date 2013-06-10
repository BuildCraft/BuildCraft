package buildcraft.energy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import buildcraft.BuildCraftCore;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.Engine;
import buildcraft.energy.TileEngine;

public abstract class GuiEngine extends GuiBuildCraft {

	protected class EngineLedger extends Ledger {

		Engine engine;
		int headerColour = 0xe1c92f;
		int subheaderColour = 0xaaafb8;
		int textColour = 0x000000;

		public EngineLedger(Engine engine) {
			this.engine = engine;
			maxHeight = 94;
			overlayColor = 0xd46c1f;
		}

		@Override
		public void draw(int x, int y) {

			// Draw background
			drawBackground(x, y);

			// Draw icon
			Minecraft.getMinecraft().renderEngine.bindTexture("/gui/items.png");
			drawIcon(BuildCraftCore.iconProvider.getIcon(CoreIconProvider.ENERGY), x + 3, y + 4);

			if (!isFullyOpened())
				return;

			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.currentOutput") + ":", x + 22, y + 20, subheaderColour);
			fontRenderer.drawString(engine.getCurrentOutput() + " MJ/t", x + 22, y + 32, textColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRenderer.drawString(engine.getEnergyStored() + " MJ", x + 22, y + 56, textColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.heat") + ":", x + 22, y + 68, subheaderColour);
			fontRenderer.drawString(((double) engine.getHeat() / (double) 100 + 20.0) + " \u00B0C", x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return engine.getCurrentOutput() + " MJ/t";
		}

	}

	public GuiEngine(BuildCraftContainer container, IInventory inventory) {
		super(container, inventory);
	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new EngineLedger(((TileEngine) tile).engine));
	}

}
