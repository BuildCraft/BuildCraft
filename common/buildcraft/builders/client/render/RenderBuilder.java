package buildcraft.builders.client.render;

import java.util.List;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.builders.tile.TileBuilder_Neptune;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public class RenderBuilder extends FastTESR<TileBuilder_Neptune> {
    @Override
    public void renderTileEntityFast(TileBuilder_Neptune te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        Box box = te.getBox();
        LaserBoxRenderer.renderLaserBoxVb(box, BuildCraftLaserManager.STRIPES_WRITE, vb);

        List<BlockPos> path = te.getPath();
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
                    Vec3d from = new Vec3d(last).add(VecUtil.VEC_HALF);
                    Vec3d to = new Vec3d(p).add(VecUtil.VEC_HALF);
                    Vec3d one = offset(from, to);
                    Vec3d two = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_PATH_CONNECTED, one, two, 1 / 16.0);
                    LaserRenderer_BC8.renderLaserBuffer(data, vb);
                }
                last = p;
            }
        }

        vb.setTranslation(0, 0, 0);
    }

    private static Vec3d offset(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, 0.125));
    }

    @Override
    public boolean isGlobalRenderer(TileBuilder_Neptune te) {
        return true;
    }
}
