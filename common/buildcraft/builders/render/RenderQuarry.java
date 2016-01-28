package buildcraft.builders.render;

import net.minecraft.util.Vec3;

import buildcraft.builders.TileQuarry;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.render.RenderBuilder;

public class RenderQuarry extends RenderBuilder<TileQuarry> {
    @Override
    public void renderTileEntityAt(TileQuarry quarry, double x, double y, double z, float partialTicks, int arg) {
        super.renderTileEntityAt(quarry, x, y, z, partialTicks, arg);
        if (quarry.arm != null) {
            if (quarry.arm.xArm != null) {
                renderCuboid(quarry, quarry.arm.xArm, x, y, z, partialTicks);
            }
            if (quarry.arm.yArm != null) {
                renderCuboid(quarry, quarry.arm.yArm, x, y, z, partialTicks);
            }
            if (quarry.arm.zArm != null) {
                renderCuboid(quarry, quarry.arm.zArm, x, y, z, partialTicks);
            }
            if (quarry.arm.headEntity != null) {
                renderCuboid(quarry, quarry.arm.headEntity, x, y, z, partialTicks);
            }
        }
    }

    private static void renderCuboid(TileQuarry quarry, EntityResizableCuboid cuboid, double x, double y, double z, float partialTicks) {
        Vec3 interp = Utils.getInterpolatedVec(cuboid, partialTicks);
        interp = interp.subtract(Utils.convert(quarry.getPos()));
        interp = interp.addVector(x, y, z);
        RenderResizableCuboid.INSTANCE.doRender(cuboid, interp.xCoord, interp.yCoord, interp.zCoord, 0, partialTicks);
    }
}
