package buildcraft.core.client.render;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.client.render.RenderMarkerHelper;
import buildcraft.lib.misc.PositionUtil;

public enum RenderMarkerPath implements IDetachedRenderer {
    INSTANCE;

    public static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_PATH_CONNECTED;

    private static final RenderMarkerHelper<TileMarkerPath> RENDER_HELPER = new RenderMarkerHelper<>(TileMarkerPath.PATH_CACHE);
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);
    private static final double SCALE = 1 / 24.05;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        RENDER_HELPER.iterateAll(player.worldObj, this::renderMarker);
    }

    private void renderMarker(TileMarkerPath marker) {
        renderPair(marker.getPos(), marker.getTo());
        renderPair(marker.getFrom(), marker.getPos());
    }

    private static void renderPair(BlockPos from, BlockPos to) {
        if (from == null || to == null) return;
        Vec3d one = new Vec3d(from).add(VEC_HALF);
        Vec3d two = new Vec3d(to).add(VEC_HALF);

        Vec3d diff = one.subtract(two).normalize();
        one = one.add(PositionUtil.scale(diff, -0.125));
        two = two.add(PositionUtil.scale(diff, 0.125));

        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
        LaserRenderer_BC8.renderLaser(data);
    }
}
