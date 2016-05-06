package buildcraft.core.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.client.RenderTickListener;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.misc.PositionUtil;

public class RenderMarkerPath extends TileEntitySpecialRenderer<TileMarkerPath> {
    public static final RenderMarkerPath INSTANCE = new RenderMarkerPath();
    public static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_PATH_CONNECTED;

    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);
    private static final double SCALE = 1 / 24.05;

    @Override
    public boolean isGlobalRenderer(TileMarkerPath te) {
        return true;
    }

    @Override
    public void renderTileEntityAt(TileMarkerPath marker, double x, double y, double z, float partialTicks, int destroyStage) {
        if (marker == null) return;
        BlockPos to = marker.getTo();
        if (to == null) return;

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("marker");
        profiler.startSection("path");

        RenderTickListener.fromPlayerPreGl(Minecraft.getMinecraft().thePlayer, partialTicks);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Vec3d one = new Vec3d(marker.getPos()).add(VEC_HALF);
        Vec3d two = new Vec3d(to).add(VEC_HALF);

        Vec3d diff = one.subtract(two).normalize();
        one = one.add(PositionUtil.scale(diff, -0.125));
        two = two.add(PositionUtil.scale(diff, 0.125));

        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
        LaserRenderer_BC8.renderLaser(data);

        RenderHelper.enableStandardItemLighting();
        RenderTickListener.fromPlayerPostGl();

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }
}
