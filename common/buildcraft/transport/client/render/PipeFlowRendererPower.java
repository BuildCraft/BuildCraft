package buildcraft.transport.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.transport.neptune.IPipeFlowRenderer;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowPower.PowerConnection;

public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        RenderHelper.disableStandardItemLighting();
        SpriteUtil.bindBlockTextureMap();
        Vec3d center = VecUtil.VEC_HALF;
        for (EnumFacing face : EnumFacing.VALUES) {
            PowerConnection type = flow.getConnectionType(face);

            Vec3d start = VecUtil.offset(center, face, 0.5);
            Vec3d end = center;//VecUtil.offset(center, face, 0);

            if (type == PowerConnection.REQUEST) {
                Vec3d t = start;
                start = end;
                end = t;
            } else if (type == PowerConnection.SENDER) {
                // Everything is already correct
            } else {
                continue;
            }
            LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.POWER_LOW, start, end, 1 / 16.0, false, false, 15);
            LaserRenderer_BC8.renderLaserStatic(data);
        }
        GlStateManager.popMatrix();
    }
}
