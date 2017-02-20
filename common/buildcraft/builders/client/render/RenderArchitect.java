package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.client.model.animation.FastTESR;

public class RenderArchitect extends FastTESR<TileArchitect> {
    @Override
    public void renderTileEntityFast(TileArchitect te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("architect");

        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        vb.setTranslation(0, 0, 0);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileArchitect te) {
        return true;
    }
}
