package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileFiller;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.client.model.animation.FastTESR;

public class RenderFiller extends FastTESR<TileFiller> {
    @Override
    public void renderTileEntityFast(TileFiller tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        vb.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, vb);

        vb.setTranslation(0, 0, 0);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
