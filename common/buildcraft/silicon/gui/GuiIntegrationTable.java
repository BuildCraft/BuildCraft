package buildcraft.silicon.gui;

import buildcraft.core.DefaultProps;
import buildcraft.silicon.TileIntegrationTable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiIntegrationTable extends GuiLaserTable {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/integration_table.png");
	private final TileIntegrationTable integrationTable;

	public GuiIntegrationTable(InventoryPlayer playerInventory, TileIntegrationTable table) {
		super(playerInventory, new ContainerIntegrationTable(playerInventory, table), table, TEXTURE);
		this.integrationTable = table;
		xSize = 175;
		ySize = 166;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		if (integrationTable.getEnergy() > 0) {
			int progress = integrationTable.getProgressScaled(24);
			drawTexturedModalRect(cornerX + 93, cornerY + 32, 176, 0, progress + 1, 18);
		}
	}
}
