package buildcraft.silicon.gui;

import buildcraft.BuildCraftCore;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;
import buildcraft.silicon.TileAdvancedCraftingTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiAdvancedCraftingTable extends GuiBuildCraft {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/assembly_advancedworkbench.png");

	class AssemblyWorkbenchLedger extends Ledger {

		int headerColour = 0xe1c92f;
		int subheaderColour = 0xaaafb8;
		int textColour = 0x000000;

		public AssemblyWorkbenchLedger() {
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

			if (!isFullyOpened())
				return;

			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.assemblyCurrentRequired") + ":", x + 22, y + 20, subheaderColour);
			fontRenderer.drawString(String.format("%2.1f MJ", workbench.getRequiredEnergy()), x + 22, y + 32, textColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRenderer.drawString(String.format("%2.1f MJ", workbench.getStoredEnergy()), x + 22, y + 56, textColour);
			fontRenderer.drawStringWithShadow(StringUtils.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
			fontRenderer.drawString(String.format("%3.2f MJ/t", workbench.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%3.2f MJ/t", workbench.getRecentEnergyAverage() / 100.0f);
		}
	}
	TileAdvancedCraftingTable workbench;

	public GuiAdvancedCraftingTable(InventoryPlayer playerInventory, TileAdvancedCraftingTable advancedWorkbench) {
		super(new ContainerAdvancedCraftingTable(playerInventory, advancedWorkbench), advancedWorkbench, TEXTURE);
		this.workbench = advancedWorkbench;
		xSize = 175;
		ySize = 240;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.assemblyWorkbenchBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);
		if (workbench.getStoredEnergy() > 0) {
			int progress = workbench.getProgressScaled(24);
			drawTexturedModalRect(cornerX + 93, cornerY + 32, 176, 0, progress + 1, 18);
		}
	}

	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new AssemblyWorkbenchLedger());
	}
}
