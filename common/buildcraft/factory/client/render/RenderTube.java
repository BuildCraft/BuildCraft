package buildcraft.factory.client.render;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.factory.tile.TileMiner;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;

public class RenderTube extends FastTESR<TileMiner> {
    private final LaserType laserType;

    public RenderTube(LaserType laserType) {
        this.laserType = laserType;
    }

    @Override
    public void renderTileEntityFast(TileMiner tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        double length = tile.getTubeLength(partialTicks);
        if (length <= 0) {
            return;
        }

        BlockPos from = tile.getPos();
        buffer.setTranslation(x - from.getX(), y - from.getY(), z - from.getZ());

        Vec3d start = new Vec3d(from.getX() + 0.5, from.getY(), from.getZ() + 0.5);

        Vec3d end = start.addVector(0, -length, 0);

        LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
        LaserRenderer_BC8.renderLaserDynamic(data, buffer);

        buffer.setTranslation(0, 0, 0);
    }
}
