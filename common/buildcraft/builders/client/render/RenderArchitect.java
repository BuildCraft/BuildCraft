package buildcraft.builders.client.render;

import net.minecraft.client.renderer.VertexBuffer;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.core.Box;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;

public class RenderArchitect extends FastTESR<TileArchitect_Neptune> {
    @Override
    public void renderTileEntityFast(TileArchitect_Neptune te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        Box box = te.getScanningBox();
        LaserBoxRenderer.renderLaserBoxVb(box, BuildCraftLaserManager.STRIPES_READ, vb);
    }
}
