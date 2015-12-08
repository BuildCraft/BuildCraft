/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import java.util.ArrayList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.MovingObjectPosition;

import buildcraft.core.lib.gui.AdvancedSlot;

class UrbanistTool {
    public TextureAtlasSprite getIcon() {
        return null;
    }

    public String getDescription() {
        return "Tool not available";
    }

    public void drawGuiContainerBackgroundLayer(GuiUrbanist gui, float f, int x, int y) {

    }

    public void drawSelection(GuiUrbanist gui, float f, int x, int y) {

    }

    public void drawGuiContainerForegroundLayer(GuiUrbanist gui, int par1, int par2) {

    }

    public boolean onInterface(int mouseX, int mouseY) {
        return false;
    }

    public void worldClicked(GuiUrbanist gui, MovingObjectPosition pos) {

    }

    public void worldMoved(GuiUrbanist gui, MovingObjectPosition pos) {

    }

    public void createSlots(GuiUrbanist gui, ArrayList<AdvancedSlot> slots) {

    }

    public void show() {

    }

    public void hide() {

    }
}
