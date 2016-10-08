package buildcraft.builders.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.builders.tile.TileFiller_Neptune;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.misc.data.Box;

public class RenderFiller extends FastTESR<TileFiller_Neptune> {
    private static final double OFFSET = 0.1;

    @Override
    public void renderTileEntityFast(TileFiller_Neptune te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        Box box = te.getBox();
        LaserBoxRenderer.renderLaserBoxVb(box, BuildCraftLaserManager.STRIPES_WRITE, vb);

        vb.setTranslation(0, 0, 0);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller_Neptune te) {
        return true;
    }
}
