package buildcraft.builders.client.render;

import net.minecraft.client.renderer.VertexBuffer;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.misc.data.Box;

public class RenderArchitect extends FastTESR<TileArchitect_Neptune> {
    @Override
    public void renderTileEntityFast(TileArchitect_Neptune te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        Box box = te.getScanningBox();
        LaserBoxRenderer.renderLaserBoxVb(box, BuildCraftLaserManager.STRIPES_READ, vb);

        vb.setTranslation(0, 0, 0);
    }

    @Override
    public boolean isGlobalRenderer(TileArchitect_Neptune te) {
        return true;
    }
}
