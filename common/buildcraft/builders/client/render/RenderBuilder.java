package buildcraft.builders.client.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public class RenderBuilder extends FastTESR<TileBuilder> {
    private static final double OFFSET = 0.1;

    @Override
    public void renderTileEntityFast(TileBuilder te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("builder");

        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        Minecraft.getMinecraft().mcProfiler.startSection("box");
        Box box = te.getBox();
        LaserBoxRenderer.renderLaserBoxDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("path");

        List<BlockPos> path = te.getPath();
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
                    Vec3d from = new Vec3d(last).add(VecUtil.VEC_HALF);
                    Vec3d to = new Vec3d(p).add(VecUtil.VEC_HALF);
                    Vec3d one = offset(from, to);
                    Vec3d two = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, one, two, 1 / 16.1);
                    LaserRenderer_BC8.renderLaserDynamic(data, vb);
                }
                last = p;
            }
        }

        Minecraft.getMinecraft().mcProfiler.endSection();

        vb.setTranslation(0, 0, 0);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static Vec3d offset(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, OFFSET));
    }

    @Override
    public boolean isGlobalRenderer(TileBuilder te) {
        return true;
    }
}
