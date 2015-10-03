/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

class UrbanistToolBlock extends UrbanistTool {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/urbanist_tool_place_block.png");
    private static final int GUI_TEXTURE_WIDTH = 64;
    private static final int GUI_TEXTURE_HEIGHT = 210;

    @Override
    public TextureAtlasSprite getIcon() {
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
        gui.getFontRenderer().drawString("Inventory", 4, 4, 0x404040);
    }

    @Override
    public boolean onInterface(int mouseX, int mouseY) {
        if (mouseX < GUI_TEXTURE_WIDTH && mouseY < GUI_TEXTURE_HEIGHT) {
            return true;
        }

        return false;
    }
}
