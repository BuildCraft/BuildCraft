/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

class UrbanistToolArea extends UrbanistTool {

    private int step = 0;
    private BlockPos start;
    private BlockPos pos;
    private float baseY = 0;

    @Override
    public TextureAtlasSprite getIcon() {
        return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Area);
    }

    @Override
    public String getDescription() {
        return "Define Area";
    }

    @Override
    public void worldClicked(GuiUrbanist gui, MovingObjectPosition pos) {
        if (step == 0) {
            this.pos = pos.getBlockPos().up();

            start = pos.getBlockPos();

            gui.urbanist.rpcCreateFrame(this.pos);

            step = 1;
        } else if (step == 1) {
            step = 2;
            baseY = (float) Mouse.getY() / (float) Minecraft.getMinecraft().displayHeight;
        } else if (step == 2) {
            step = 0;

            areaSet(gui, start, this.pos);
        }
    }

    public void areaSet(GuiUrbanist urbanist, BlockPos start, BlockPos end) {

    }

    @Override
    public void worldMoved(GuiUrbanist gui, MovingObjectPosition pos) {
        if (step == 1) {
            this.pos = new BlockPos(pos.getBlockPos().getX(), this.pos.getY(), pos.getBlockPos().getX());

            gui.urbanist.rpcMoveFrame(this.pos);
        } else if (step == 2) {
            float ydiff = (float) Mouse.getY() / (float) Minecraft.getMinecraft().displayHeight;

            this.pos = new BlockPos(this.pos.getX(), (int) (start.getY() + (ydiff - baseY) * 50), this.pos.getZ());

            gui.urbanist.rpcMoveFrame(this.pos);
        }
    }
}
