package buildcraft.transport.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.client.render.RenderResizableCuboid;
import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipeTransportRendererPower extends PipeTransportRenderer<PipeTransportPower> {
    /** The number of pixels the power moves by per millisecond */
    public static final double FLOW_MULTIPLIER = 0.048;
    public static final short POWER_STAGES = PipeTransportPower.POWER_STAGES;

    @Override
    public void render(Pipe<PipeTransportPower> pipe, double x, double y, double z, float f) {
        PipeTransportPower pow = pipe.transport;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GlStateManager.disableLighting();
        // GL11.glEnable(GL11.GL_BLEND);

        GL11.glTranslatef((float) x, (float) y, (float) z);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        // Used for the centre rendering
        double centerPower = 0;
        double[] power = new double[6];
        short[] flow = pow.displayFlow;
        for (int i = 0; i < 6; i++) {
            power[i] = pow.displayPowerAverage[i].getAverage();
            double d = power[i];
            if (d > centerPower) {
                centerPower = d;
            }
        }

        if (centerPower > 0) {
            long ms = System.currentTimeMillis();
            long diff = ms - pow.clientLastDisplayTime;
            if (pow.clientLastDisplayTime == 0 || diff <= 0) {
                diff = 1;
            }
            pow.clientLastDisplayTime = ms;

            for (int i = 0; i < 6; i++) {
                EnumFacing face = EnumFacing.values()[i];
                if (!pipe.getTile().isPipeConnected(face)) {
                    continue;
                }
                double actualDiff = flow[i] * diff * FLOW_MULTIPLIER;
                double connectionDiff = face.getAxisDirection() == AxisDirection.POSITIVE ? actualDiff : -actualDiff;
                pow.clientDisplayFlow[i] += connectionDiff;
                while (pow.clientDisplayFlow[i] < 0) {
                    pow.clientDisplayFlow[i] += 16;
                }
                while (pow.clientDisplayFlow[i] > 16) {
                    pow.clientDisplayFlow[i] -= 16;
                }

                pow.clientDisplayFlowCentre = pow.clientDisplayFlowCentre.add(Utils.convert(face, actualDiff / 2));
                renderSidePower(face, power[i], pow.clientDisplayFlow[i], centerPower);
            }

            for (Axis axis : Axis.values()) {
                double value = Utils.getValue(pow.clientDisplayFlowCentre, axis);
                while (value < 0) {
                    value += 16;
                }
                while (value > 16) {
                    value -= 16;
                }
                pow.clientDisplayFlowCentre = Utils.withValue(pow.clientDisplayFlowCentre, axis, value);
            }
            renderCenterPower(centerPower, pow.clientDisplayFlowCentre);
        }

        GlStateManager.enableLighting();

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private static void renderSidePower(EnumFacing face, double stage, double flow, double centerStage) {
        if (stage <= 0) {
            return;
        }

        double width = 0.5 * stage / POWER_STAGES;
        double centerRadius = 0.25 * centerStage / POWER_STAGES;

        Vec3d center = Utils.VEC_HALF.add(Utils.convert(face, 0.25 + centerRadius / 2d));

        face = Utils.convertPositive(face);
        Vec3d size = Utils.VEC_ONE.subtract(Utils.convert(face));
        size = Utils.multiply(size, width);
        size = size.add(Utils.convert(face, 0.5 - centerRadius));

        EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
        cuboid.setSize(size);
        cuboid.texture = PipeIconProvider.TYPE.Power_Normal.getIcon();
        cuboid.makeClient();

        double offsetNonFlow = 0;// 8 - textureWidth / 2;
        double offsetFlow = flow;

        Vec3d textureOffset = new Vec3d(offsetNonFlow, offsetNonFlow, offsetNonFlow);
        textureOffset = textureOffset.add(Utils.convert(face, -offsetNonFlow));
        textureOffset = textureOffset.add(Utils.convert(face, offsetFlow));

        cuboid.textureOffsetX = textureOffset.xCoord;
        cuboid.textureOffsetY = textureOffset.yCoord;
        cuboid.textureOffsetZ = textureOffset.zCoord;

        GL11.glPushMatrix();
        RenderUtils.translate(center);
        // Tessellator.getInstance().getWorldRenderer().setBrightness(0xFFFFFFFF);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xF0, 0xF0);
        RenderResizableCuboid.INSTANCE.renderCubeFromCentre(cuboid);
        GL11.glPopMatrix();
    }

    private static void renderCenterPower(double stage, Vec3d centerFlow) {
        if (stage <= 0) {
            return;
        }
        double width = 0.5 * stage / POWER_STAGES;

        Vec3d size = new Vec3d(width, width, width);
        Vec3d pos = Utils.VEC_HALF;

        EntityResizableCuboid erc = new EntityResizableCuboid(null);
        erc.setSize(size);
        erc.texture = PipeIconProvider.TYPE.Power_Normal.getIcon();

        erc.textureOffsetX = centerFlow.xCoord;
        erc.textureOffsetY = centerFlow.yCoord;
        erc.textureOffsetZ = centerFlow.zCoord;

        GL11.glPushMatrix();
        RenderUtils.translate(pos);
        // Tessellator.getInstance().getWorldRenderer().setBrightness(0xFFFFFFFF);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xF0, 0xF0);
        RenderResizableCuboid.INSTANCE.renderCubeFromCentre(erc);
        GL11.glPopMatrix();
    }
}
