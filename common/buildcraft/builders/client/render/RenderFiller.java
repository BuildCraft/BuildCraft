package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileFiller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.client.model.animation.FastTESR;

import javax.annotation.Nonnull;

public class RenderFiller extends FastTESR<TileFiller> {
    @Override
    public void renderTileEntityFast(@Nonnull TileFiller tile, double x, double y, double z, float partialTicks, int destroyStage, @Nonnull VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("filler");

        RenderSnapshotBuilder.render(tile.builder, tile.getWorld(), tile.getPos(), x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
