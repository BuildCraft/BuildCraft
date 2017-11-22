package buildcraft.factory.client.render;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer.TankSize;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileHeatExchange.EnumProgressState;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import buildcraft.factory.tile.TileHeatExchange.ExchangeSectionStart;

public class RenderHeatExchange extends TileEntitySpecialRenderer<TileHeatExchange> {
    private static final Map<EnumFacing, TankSideData> TANK_SIDES = new EnumMap<>(EnumFacing.class);
    private static final TankSize TANK_BOTTOM, TANK_TOP;

    static {
        double s = 1 / 64.0;
        TANK_BOTTOM = new TankSize(2, 0, 2, 14, 2, 14).shrink(s, 0, s);
        TANK_TOP = new TankSize(2, 14, 2, 14, 16, 14).shrink(s, 0, s);
        TankSize start = new TankSize(0, 4, 4, 2, 12, 12).shrink(0, s, s);
        TankSize end = new TankSize(14, 4, 4, 16, 12, 12).shrink(0, s, s);
        TankSideData sides = new TankSideData(start, end);
        EnumFacing face = EnumFacing.EAST;
        for (int i = 0; i < 4; i++) {
            TANK_SIDES.put(face, sides);
            face = face.rotateY();
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

    @Override
    public void render(TileHeatExchange tile, double x, double y, double z, float partialTicks, int destroyStage,
        float alpha) {
        super.render(tile, x, y, z, partialTicks, destroyStage, alpha);

        if (!tile.isStart()) {
            return;
        }

        ExchangeSectionStart section = (ExchangeSectionStart) tile.getSection();
        ExchangeSectionEnd sectionEnd = section.getEndSection();

        IBlockState state = tile.getCurrentStateForBlock(BCFactoryBlocks.heatExchange);
        if (state == null) {
            return;
        }

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("heat_exchange");

        int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), 0);

        // gl state setup
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        // buffer setup
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        bb.setTranslation(x, y, z);

        profiler.startSection("tank");

        EnumFacing face = state.getValue(BlockBCBase_Neptune.PROP_FACING).rotateYCCW();
        TankSideData sideTank = TANK_SIDES.get(face);

        renderTank(TANK_BOTTOM, section.smoothedTankInput, combinedLight, partialTicks, bb);
        renderTank(sideTank.start, section.smoothedTankOutput, combinedLight, partialTicks, bb);

        int middles = section.middleCount;
        if (sectionEnd != null) {
            // TODO: Move this into the other renderer!
            BlockPos diff = sectionEnd.tile.getPos().subtract(tile.getPos());
            bb.setTranslation(x + diff.getX(), y + diff.getY(), z + diff.getZ());
            renderTank(TANK_TOP, sectionEnd.smoothedTankOutput, combinedLight, partialTicks, bb);
            renderTank(sideTank.end, sectionEnd.smoothedTankInput, combinedLight, partialTicks, bb);
            bb.setTranslation(x, y, z);
        }

        profiler.endStartSection("flow");

        if (middles > 0) {
            EnumProgressState progressState = section.getProgressState();
            double progress = section.getProgress(partialTicks);
            if (progress > 0) {
                double length = middles + 1 - 4 / 16.0 - 0.02;
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
                BlockPos diff = BlockPos.ORIGIN;
                if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
                    diff = diff.offset(face, middles);
                }
                double otherStart = flip ? p0 : p1 - length * progress;
                double otherEnd = flip ? p0 + length * progress : p1;
                Vec3d vDiff = new Vec3d(diff).addVector(x, y, z);
                renderFlow(vDiff, face, bb, progressStart + 0.01, progressEnd - 0.01,
                    sectionEnd.smoothedTankInput.getFluidForRender(), 4, partialTicks);
                renderFlow(vDiff, face.getOpposite(), bb, otherStart, otherEnd,
                    section.smoothedTankInput.getFluidForRender(), 2, partialTicks);
            }
        }

        // buffer finish
        bb.setTranslation(0, 0, 0);
        profiler.endStartSection("draw");
        Tessellator.getInstance().draw();

        // gl state finish
        RenderHelper.enableStandardItemLighting();

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }

    private static void renderTank(TankSize size, FluidSmoother tank, int combinedLight, float partialTicks,
        BufferBuilder bb) {
        FluidStackInterp fluid = tank.getFluidForRender(partialTicks);
        if (fluid == null || fluid.amount <= 0) {
            return;
        }
        int blockLight = fluid.fluid.getFluid().getLuminosity(fluid.fluid) & 0xF;
        combinedLight |= blockLight << 4;
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid.fluid, fluid.amount, tank.getCapacity(), size.min,
            size.max, bb, null);
    }

    private static void renderFlow(Vec3d diff, EnumFacing face, BufferBuilder bb, double s, double e, FluidStack fluid,
        int point, float partialTicks) {
        double tickTime = Minecraft.getMinecraft().world.getTotalWorldTime();
        double offset = (tickTime + partialTicks) % 31 / 31.0;
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            offset = -offset;
            face = face.getOpposite();
        }
        Vec3d dirVec = new Vec3d(face.getDirectionVec());
        double ds = (point + 0.1) / 16.0;
        Vec3d vs = new Vec3d(ds, ds, ds);
        Vec3d ve = new Vec3d(1 - ds, 1 - ds, 1 - ds);
        diff = diff.subtract(VecUtil.scale(dirVec, offset));
        s += offset;
        e += offset;
        if (s < 0) {
            s++;
            e++;
            diff = diff.subtract(dirVec);
        }
        for (int i = 0; i <= e; i++) {
            Vec3d d = diff;
            diff = diff.add(dirVec);
            if (i < s - 1) {
                continue;
            }
            bb.setTranslation(d.x, d.y, d.z);

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
            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, fluid, 1, 1, vs, ve, bb, sides);

        }
    }

    @Override
    public boolean isGlobalRenderer(TileHeatExchange tile) {
        return tile.isStart();
    }
}
