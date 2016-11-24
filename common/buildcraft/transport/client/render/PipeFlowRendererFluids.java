package buildcraft.transport.client.render;

import java.util.Arrays;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.neptune.IPipeFlowRenderer;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.pipe.flow.PipeFlowFluids;

public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
    INSTANCE;

    private static final Vec3d[] MIN_FULL;// TEMP
    private static final Vec3d[] MAX_FULL;// TEMP

    static {
        MIN_FULL = new Vec3d[7];
        MAX_FULL = new Vec3d[7];

        for (EnumFacing face : EnumFacing.VALUES) {
            Vec3d faceVec = new Vec3d(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
            Vec3d center = new Vec3d(0.5, 0.5, 0.5).add(VecUtil.scale(faceVec, 0.37));
            Vec3d radius = new Vec3d(0.24, 0.24, 0.24);
            radius = VecUtil.replaceValue(radius, face.getAxis(), 0.13);

            MIN_FULL[face.ordinal()] = center.subtract(radius);
            MAX_FULL[face.ordinal()] = center.add(radius);
        }
    }

    @Override
    public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        FluidStack forRender = flow.getFluidStackForRender();
        if (forRender == null) {
            return;
        }

        double[] amounts = flow.getAmountsForRender(partialTicks);

        VertexBuffer fluidBuffer = vb;
        // fluidBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        fluidBuffer.setTranslation(x, y, z);

        for (EnumFacing face : EnumFacing.VALUES) {
            boolean[] sides = new boolean[6];
            Arrays.fill(sides, true);
            // sides[face.ordinal()] = false;
            // sides[face.getOpposite().ordinal()] = false;
            Vec3d min = MIN_FULL[face.ordinal()];
            Vec3d max = MAX_FULL[face.ordinal()];

            FluidRenderer.renderFluid(FluidSpriteType.STILL, forRender, amounts[face.getIndex()], flow.capacity, min, max, fluidBuffer, sides);
        }

        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);
        // for (EnumFacing face : EnumFacing.VALUES) {
        // sides[face.ordinal()] = !flow.pipe.isConnected(face);
        // }
        double amount = amounts[EnumPipePart.CENTER.getIndex()];
        Vec3d min = new Vec3d(0.26, 0.26, 0.26);
        Vec3d max = new Vec3d(0.74, 0.74, 0.74);

        FluidRenderer.renderFluid(FluidSpriteType.STILL, forRender, amount, flow.capacity, min, max, fluidBuffer, sides);

        // gl state setup
        // RenderHelper.disableStandardItemLighting();
        // Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        // GlStateManager.enableBlend();
        // GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        // Tessellator.getInstance().draw();

        // RenderHelper.enableStandardItemLighting();

        fluidBuffer.setTranslation(0, 0, 0);
    }
}
