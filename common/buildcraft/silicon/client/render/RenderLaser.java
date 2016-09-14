package buildcraft.silicon.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.silicon.tile.TileLaser;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.FastTESR;

public class RenderLaser extends FastTESR<TileLaser> {
    @Override
    public void renderTileEntityFast(TileLaser tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        if(tile.laserPos != null) {
            EnumFacing side = tile.getWorld().getBlockState(tile.getPos()).getValue(BuildCraftProperties.BLOCK_FACING_6);
            Vec3d offset = new Vec3d(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(2 / 16D));
            int index = (int) ((double) tile.getAverage() / tile.getMaxPowerPerTick() * (BuildCraftLaserManager.POWERS.length - 1));
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(BuildCraftLaserManager.POWERS[index], new Vec3d(tile.getPos()).add(offset), tile.laserPos, 1 / 16D), buffer);
        }

        buffer.setTranslation(0, 0, 0);
    }
}
