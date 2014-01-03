package buildcraft.silicon.gui;

import buildcraft.core.DefaultProps;
import buildcraft.silicon.TileAdvancedCraftingTable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiAdvancedCraftingTable extends GuiLaserTable {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/assembly_advancedworkbench.png");
	private final TileAdvancedCraftingTable workbench;

	public GuiAdvancedCraftingTable(InventoryPlayer playerInventory, TileAdvancedCraftingTable advancedWorkbench) {
		super(playerInventory, new ContainerAdvancedCraftingTable(playerInventory, advancedWorkbench), advancedWorkbench, TEXTURE);
		this.workbench = advancedWorkbench;
		xSize = 175;
		ySize = 240;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		if (workbench.getEnergy() > 0) {
			int progress = workbench.getProgressScaled(24);
			drawTexturedModalRect(cornerX + 93, cornerY + 32, 176, 0, progress + 1, 18);
		}
	}
}
