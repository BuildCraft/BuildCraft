package buildcraft.factory.client.render;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileHeatExchange.EnumProgressState;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionStart;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer.TankSize;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.misc.VecUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RenderHeatExchange extends TileEntityRenderer<TileHeatExchange> {
    private static final Map<Direction, TankSideData> TANK_SIDES = new EnumMap<>(Direction.class);
    private static final TankSize TANK_BOTTOM, TANK_TOP;

    static {
        double s = 1 / 64.0;
        TANK_BOTTOM = new TankSize(2, 0, 2, 14, 2, 14).shrink(s, 0, s);
        TANK_TOP = new TankSize(2, 14, 2, 14, 16, 14).shrink(s, 0, s);
        TankSize start = new TankSize(0, 4, 4, 2, 12, 12).shrink(0, s, s);
        TankSize end = new TankSize(14, 4, 4, 16, 12, 12).shrink(0, s, s);
        TankSideData sides = new TankSideData(start, end);
        Direction face = Direction.EAST;
        for (int i = 0; i < 4; i++) {
            TANK_SIDES.put(face, sides);
            face = face.getClockWise();
            sides = sides.rotateY();
        }
    }

    static class TankSideData {
        public final TankSize start, end;

        public TankSideData(TankSize start, TankSize end) {
            this.start = start;
            this.end = end;
        }

        public TankSideData rotateY() {
            return new TankSideData(start.rotateY(), end.rotateY());
        }
    }

    public RenderHeatExchange(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public void render(TileHeatExchange tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    public void render(TileHeatExchange tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
        if (!tile.isStart()) {
            return;
        }

        ExchangeSectionStart section = (ExchangeSectionStart) tile.getSection();
        ExchangeSectionEnd sectionEnd = section.getEndSection();

        BlockState state = tile.getCurrentStateForBlock(BCFactoryBlocks.heatExchange.get());
        if (state == null) {
            return;
        }

        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.push("bc");
        profiler.push("heat_exchange");

        // 1.18.2: provided
//        int combinedLight = tile.getLevel().getLightEngine().getRawBrightness(tile.getBlockPos(), 0);

//        // gl state setup
//        RenderHelper.disableStandardItemLighting();
//        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        // buffer setup
//        try (AutoTessellator tess = RenderUtil.getThreadLocalUnusedTessellator()) {
//            BufferBuilder bb = tess.tessellator.getBuffer();
//            bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//            bb.setTranslation(x, y, z);
        IVertexBuilder bb = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());

        profiler.push("tank");

        Direction face = state.getValue(BlockBCBase_Neptune.PROP_FACING).getCounterClockWise();
        TankSideData sideTank = TANK_SIDES.get(face);

        renderTank(TANK_BOTTOM, section.smoothedTankInput, combinedLight, combinedOverlay, partialTicks, poseStack.last(), bb);
        renderTank(sideTank.start, section.smoothedTankOutput, combinedLight, combinedOverlay, partialTicks, poseStack.last(), bb);

        int middles = section.middleCount;
        if (sectionEnd != null) {
            // TODO: Move this into the other renderer!
            BlockPos diff = sectionEnd.getTile().getBlockPos().subtract(tile.getBlockPos());
            poseStack.pushPose();
//            bb.setTranslation(x + diff.getX(), y + diff.getY(), z + diff.getZ());
            poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
            renderTank(TANK_TOP, sectionEnd.smoothedTankOutput, combinedLight, combinedOverlay, partialTicks, poseStack.last(), bb);
            renderTank(sideTank.end, sectionEnd.smoothedTankInput, combinedLight, combinedOverlay, partialTicks, poseStack.last(), bb);
//            bb.setTranslation(x, y, z);
            poseStack.popPose();
        }

        profiler.popPush("flow");

        if (middles > 0 && sectionEnd != null) {
            EnumProgressState progressState = section.getProgressState();
            double progress = section.getProgress(partialTicks);
            if (progress > 0) {
                double length = middles + 2 - 4 / 16.0 - 0.02;
                double p0 = 2 / 16.0 + 0.01;
                double p1 = p0 + length - 0.01;
                double progressStart = p0;
                double progressEnd = p0 + length * progress;

                boolean flip = progressState == EnumProgressState.PREPARING;
                flip ^= face.getAxisDirection() == AxisDirection.NEGATIVE;

                if (flip) {
                    progressStart = p1 - length * progress;
                    progressEnd = p1;
                }
                BlockPos diff = BlockPos.ZERO;
                if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
                    diff = diff.relative(face, middles + 1);
                }
                double otherStart = flip ? p0 : p1 - length * progress;
                double otherEnd = flip ? p0 + length * progress : p1;
                Vector3d vDiff = Vector3d.atLowerCornerOf(diff);
                // Inner Fluid
                IVertexBuilder bbForInnerFlow = bufferSource.getBuffer(RenderType.solid());
                renderFlow(vDiff, face, poseStack, bbForInnerFlow, progressStart + 0.01, progressEnd - 0.01,
                        sectionEnd.smoothedTankInput.getFluidForRender(), combinedLight, combinedOverlay, 4, partialTicks);
                // Outer Fluid
                IVertexBuilder bbForOuterFlow = bufferSource.getBuffer(RenderType.translucent());
                renderFlow(vDiff, face.getOpposite(), poseStack, bbForOuterFlow, otherStart, otherEnd,
                        section.smoothedTankInput.getFluidForRender(), combinedLight, combinedOverlay, 2, partialTicks);
            }
        }

        // buffer finish
//            bb.setTranslation(0, 0, 0);
        profiler.popPush("draw");
//            tess.tessellator.draw();
//        }

//        // gl state finish
//        RenderHelper.enableStandardItemLighting();

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }

    private static void renderTank(
            TankSize size,
            FluidSmoother tank,
            int combinedLight,
            int combinedOverlay,
            float partialTicks,
            Entry pose,
            IVertexBuilder bb
    ) {
        FluidStackInterp fluid = tank.getFluidForRender(partialTicks);
        if (fluid == null || fluid.amount <= 0) {
            return;
        }
        int blockLight = fluid.fluid.getRawFluid().getAttributes().getLuminosity(fluid.fluid) & 0xF;
        combinedLight |= blockLight << 4;
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.vertex.overlay(combinedOverlay);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid.fluid, fluid.amount, tank.getCapacity(), size.min,
                size.max, pose, bb, null);
    }

    private static void renderFlow(
            Vector3d diff,
            Direction face,
            MatrixStack poseStack,
            IVertexBuilder bb,
            double s, double e,
            FluidStack fluid,
            int combinedLight,
            int combinedOverlay,
            int point,
            float partialTicks
    ) {
//        double tickTime = Minecraft.getMinecraft().world.getTotalWorldTime();
        double tickTime = Minecraft.getInstance().level.getGameTime();
        double offset = (tickTime + partialTicks) % 31 / 31.0;
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            offset = -offset;
            face = face.getOpposite();
        }
        Vector3d dirVec = Vector3d.atLowerCornerOf(face.getNormal());
        double ds = (point + 0.1) / 16.0;
        Vector3d vs = new Vector3d(ds, ds, ds);
        Vector3d ve = new Vector3d(1 - ds, 1 - ds, 1 - ds);
        diff = diff.subtract(VecUtil.scale(dirVec, offset));
        s += offset;
        e += offset;
        if (s < 0) {
            s++;
            e++;
            diff = diff.subtract(dirVec);
        }
        for (int i = 0; i <= e; i++) {
            Vector3d d = diff;
            diff = diff.add(dirVec);
            if (i < s - 1) {
                continue;
            }
            poseStack.pushPose();
//            bb.setTranslation(d.x, d.y, d.z);
            poseStack.translate(d.x, d.y, d.z);

            double s1 = s < i ? 0 : (s % 1);
            double e1 = e > i + 1 ? 1 : (e % 1);
            vs = VecUtil.replaceValue(vs, face.getAxis(), s1);
            ve = VecUtil.replaceValue(ve, face.getAxis(), e1);
            boolean[] sides = new boolean[6];
            Arrays.fill(sides, true);
            if (s < i) {
                sides[face.getOpposite().ordinal()] = false;
            }
            if (e > i + 1) {
                sides[face.ordinal()] = false;
            }
            // Calen FIX: without the light, the flow of amount 0 will be dark, the water outside of lava will be light, which appears in 1.12.2, that seems wrong
            int blockLight = fluid.getRawFluid().getAttributes().getLuminosity(fluid) & 0xF;
            combinedLight |= blockLight << 4;
            FluidRenderer.vertex.lighti(combinedLight);
            FluidRenderer.vertex.overlay(combinedOverlay);
            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, fluid, 1, 1, vs, ve, poseStack.last(), bb, sides);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileHeatExchange tile) {
        return tile.isStart();
    }
}
