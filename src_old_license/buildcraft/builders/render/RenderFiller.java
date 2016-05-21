package buildcraft.builders.render;

import net.minecraft.client.Minecraft;

import buildcraft.builders.TileFiller;
import buildcraft.core.render.RenderBuilder;

public class RenderFiller extends RenderBuilder<TileFiller> {
    @Override
    public void renderTileEntityAt(TileFiller builder, double x, double y, double z, float f, int arg) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        super.renderTileEntityAt(builder, x, y, z, f, arg);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
