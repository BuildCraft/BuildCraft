package buildcraft.factory.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.Vec3;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.client.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.Utils;
import buildcraft.factory.TilePump;

public class RenderPump extends TileEntitySpecialRenderer<TilePump> {
    @Override
    public void renderTileEntityAt(TilePump pump, double x, double y, double z, float partialTicks, int destroyStage) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pump");
        Minecraft.getMinecraft().mcProfiler.startSection("tube");
        if (pump.tube != null) {
            EntityResizableCuboid cuboid = pump.tube;
            Vec3 interp = Utils.getInterpolatedVec(cuboid, partialTicks);
            interp = interp.subtract(Utils.convert(pump.getPos()));
            interp = interp.addVector(x, y, z);
            RenderResizableCuboid.INSTANCE.doRender(cuboid, interp.xCoord, interp.yCoord, interp.zCoord, 0, partialTicks);
        }
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
