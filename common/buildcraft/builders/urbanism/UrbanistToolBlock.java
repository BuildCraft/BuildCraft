package buildcraft.builders.urbanism;

import buildcraft.core.DefaultProps;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

class UrbanistToolBlock extends UrbanistTool {

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/urbanist_tool_place_block.png");
	private static final int GUI_TEXTURE_WIDTH = 64;
	private static final int GUI_TEXTURE_HEIGHT = 210;

	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Block_Place);
	}

	@Override
	public String getDescription() {
		return "Place Block";
	}

	@Override
	public void drawGuiContainerBackgroundLayer(GuiUrbanist gui, float f, int x, int y) {
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);
		gui.drawTexturedModalRect(0, 0, 0, 0, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
	}

	@Override
	public void drawGuiContainerForegroundLayer(GuiUrbanist gui, int par1, int par2) {
		gui.getFontRenderer ().drawString("Inventory", 4, 4, 0x404040);
	}

	@Override
	public boolean onInterface (int mouseX, int mouseY) {
		if (mouseX < GUI_TEXTURE_WIDTH && mouseY < GUI_TEXTURE_HEIGHT) {
			return true;
		}

		return false;
	}
}