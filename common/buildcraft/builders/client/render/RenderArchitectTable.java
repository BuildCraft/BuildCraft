package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.client.model.animation.FastTESR;

import javax.annotation.Nonnull;

public class RenderArchitectTable extends FastTESR<TileArchitectTable> {
    @Override
    public void renderTileEntityFast(@Nonnull TileArchitectTable te, double x, double y, double z, float partialTicks, int destroyStage, @Nonnull VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("architect");

        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        vb.setTranslation(0, 0, 0);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileArchitectTable te) {
        return true;
    }
}
