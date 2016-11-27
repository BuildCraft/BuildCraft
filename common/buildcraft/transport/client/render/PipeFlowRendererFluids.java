package buildcraft.transport.client.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.neptune.IPipeFlowRenderer;
import buildcraft.api.transport.neptune.IPipeHolder;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.pipe.Pipe;
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

        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);

        double[] amounts = flow.getAmountsForRender(partialTicks);
        Vec3d[] offsets = flow.getOffsetsForRender(partialTicks);

        int blocklight = forRender.getFluid().getLuminosity(forRender);
        IPipeHolder holder = flow.pipe.getHolder();
        int combinedLight = holder.getPipeWorld().getCombinedLight(holder.getPipePos(), blocklight);

        FluidRenderer.vertex.lighti(combinedLight);

        VertexBuffer fluidBuffer = Tessellator.getInstance().getBuffer();
        fluidBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        fluidBuffer.setTranslation(x, y, z);

        for (EnumFacing face : EnumFacing.VALUES) {
            double size = ((Pipe) flow.pipe).getConnectedDist(face);

            Vec3d center = VecUtil.offset(new Vec3d(0.5, 0.5, 0.5), face, 0.245 + size / 2);
            Vec3d radius = new Vec3d(0.24, 0.24, 0.24);
            radius = VecUtil.replaceValue(radius, face.getAxis(), 0.005 + size / 2);

            Vec3d offset = offsets[face.getIndex()];
            if (offset == null) offset = Vec3d.ZERO;
            center = center.add(offset);
            fluidBuffer.setTranslation(x - offset.xCoord, y - offset.yCoord, z - offset.zCoord);

            Vec3d min = center.subtract(radius);
            Vec3d max = center.add(radius);

            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amounts[face.getIndex()], flow.capacity, min, max, fluidBuffer, sides);
        }
        double amount = amounts[EnumPipePart.CENTER.getIndex()];

        boolean horizontal = true;
        boolean vertical = false;
        double horizPos = 0.25;

        if (horizontal | !vertical) {
            Vec3d min = new Vec3d(0.26, 0.26, 0.26);
            Vec3d max = new Vec3d(0.74, 0.74, 0.74);

            Vec3d offset = offsets[EnumPipePart.CENTER.getIndex()];
            if (offset == null) offset = Vec3d.ZERO;
            min = min.add(offset);
            max = max.add(offset);
            fluidBuffer.setTranslation(x - offset.xCoord, y - offset.yCoord, z - offset.zCoord);

            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, fluidBuffer, sides);
        }

        if (vertical) {
            if (horizPos <= 0.25) {
                // draw the bottom face
            }
        }

        // gl state setup
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableCull();

        fluidBuffer.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();

        RenderHelper.enableStandardItemLighting();

        FluidRenderer.vertex.lighti(0xF, 0xF);

    }

    private static void drawFluidCenter(FluidStack fluid, double percentage, boolean horizontal, boolean above, VertexBuffer vb) {
        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);
        Vec3d min = new Vec3d(0.26, 0.26, 0.26);
        Vec3d max = new Vec3d(0.74, 0.74, 0.74);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid, percentage, 1, min, max, vb, sides);
    }
}
