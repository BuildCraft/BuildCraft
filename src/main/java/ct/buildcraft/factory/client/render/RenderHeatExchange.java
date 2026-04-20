package ct.buildcraft.factory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import ct.buildcraft.factory.block.BlockHeatExchange;
import ct.buildcraft.factory.tile.TileHeatExchange;
import ct.buildcraft.factory.tile.TileHeatExchange.EnumProgressState;
import ct.buildcraft.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import ct.buildcraft.factory.tile.TileHeatExchange.ExchangeSectionStart;
import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.client.render.fluid.FluidSpriteType;
import ct.buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import ct.buildcraft.lib.misc.VecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

public class RenderHeatExchange implements BlockEntityRenderer<TileHeatExchange>{

    public RenderHeatExchange(BlockEntityRendererProvider.Context ctx) {
    	mc = Minecraft.getInstance();
    }
    
    protected static final Vec3 INLINE_MIN = new Vec3(0,0.25+0.001,0.25+0.001);
    protected static final Vec3 INLINE_MAX = new Vec3(0.125,0.75-0.001,0.75-0.001);
    protected static final Vec3 OUTLINE_MIN = new Vec3(0.125+0.001,0.875+0.001,0.125+0.001);
    protected static final Vec3 OUTLINE_MAX = new Vec3(0.875-0.001,1-0.001,0.875-0.001);
    protected static final boolean[] sideRender = { true, true, true, true, true, true };
	private static Minecraft mc;
    

	@Override
	public void render(TileHeatExchange tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
		var section = tile.getSection();
		if(section == null)  return;
		matrix.pushPose();
		Direction face = tile.getBlockState().getValue(BlockHeatExchange.PROP_FACING).getCounterClockWise();
		if(section instanceof ExchangeSectionStart start) {
			FluidStackInterp input = start.smoothedTankInput.getFluidForRender(partialTicks);
			FluidStackInterp output = start.smoothedTankOutput.getFluidForRender(partialTicks);
			VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
			ExchangeSectionEnd sectionEnd = start.getEndSection();
			int middles = start.middleCount;
			if (middles > 0 && sectionEnd != null) {
                EnumProgressState progressState = start.getProgressState();
                double progress = start.getProgress(partialTicks);
                if (progress > 0) {
                    double length = middles + 2 - 4 / 16.0 - 0.02;
                    double p0 = 2 / 16.0 + 0.01;
                    double p1 = p0 + length - 0.01;
                    double progressStart = p0;
                    double progressEnd = p0 + length * progress;
                    boolean flip = progressState == EnumProgressState.PREPARING;
                    
                    boolean cache = flip&progress<0.016;
                    FluidStack fluidstart = start.getInputFluidForRender(cache);
                    FluidType typestart = start.getInputFluidType();
                    FluidStack fluidend = sectionEnd.getInputFluidForRender(cache);
                    FluidType typeend = sectionEnd.getInputFluidType();
                    
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
                    Vec3 vDiff = Vec3.atLowerCornerOf(diff);
                    renderFlow(vDiff, face, matrix, bb, progressStart + 0.01, progressEnd - 0.01,
                    		fluidend , typeend, 4, partialTicks, light);
                    renderFlow(vDiff, face.getOpposite(), matrix, bb, otherStart, otherEnd,
                    		fluidstart , typestart, 2, partialTicks, light);
                }
    			matrix.translate(0.5, 0.5, 0.5);
    	        switch(face) {
    			case NORTH:
    				matrix.mulPose(Vector3f.YP.rotationDegrees(90));
    				break;
    			case WEST:
    				matrix.mulPose(Vector3f.YN.rotationDegrees(180));
    				break;
    			case SOUTH:
    				matrix.mulPose(Vector3f.YN.rotationDegrees(90));
    			default:
    				break;
    	        }
    			matrix.translate(-0.5, -0.5, -0.5);
    			renderInline(output, partialTicks, matrix, bb, light, overlay);
    			matrix.translate(0, -0.875, 0);
    			renderOutline(input, partialTicks, matrix, bb, light, overlay);
    		
			}
		}
		else if(section instanceof ExchangeSectionEnd end) {
			FluidStackInterp input = end.smoothedTankInput.getFluidForRender(partialTicks);
			FluidStackInterp output = end.smoothedTankOutput.getFluidForRender(partialTicks);
			VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
			matrix.translate(0.5, 0.5, 0.5);
	        switch(face) {
			case NORTH:
				matrix.mulPose(Vector3f.YP.rotationDegrees(90));
				break;
			case WEST:
				matrix.mulPose(Vector3f.YN.rotationDegrees(180));
				break;
			case SOUTH:
				matrix.mulPose(Vector3f.YN.rotationDegrees(90));
			default:
				break;
	        }
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));
			matrix.translate(-0.5, -0.5, -0.5);
			renderInline(input, partialTicks, matrix, bb, light, overlay);
			renderOutline(output, partialTicks, matrix, bb, light, overlay);
		}
		
		

		matrix.popPose();
	}
	
	private void renderInline(FluidStackInterp forRender, float partialTicks, PoseStack matrix, VertexConsumer bb, int light, int overlay) {
		if(forRender ==null)
			return;
		FluidStack fluid = forRender.fluid;
        int blocklight0 = light&0x0000F0;
        int skylight0 = light&0xF00000;
        int blocklight = fluid.getFluid().getFluidType().getLightLevel(fluid)<<4;
        blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
        int combinedLight = (skylight0)+(blocklight);
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid, forRender.amount, 2000, INLINE_MIN, INLINE_MAX,
            bb, matrix.last(), sideRender);
	}
	
	private void renderOutline(FluidStackInterp forRender, float partialTicks, PoseStack matrix, VertexConsumer bb, int light, int overlay) {
		if(forRender == null)
			return;
		FluidStack fluid = forRender.fluid;
        int blocklight0 = light&0x0000F0;
        int skylight0 = light&0xF00000;
        int blocklight = fluid.getFluid().getFluidType().getLightLevel(fluid)<<4;
        blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
        int combinedLight = (skylight0)+(blocklight);
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid, forRender.amount, 2000, OUTLINE_MIN, OUTLINE_MAX,
            bb, matrix.last(), sideRender);
	}
	
    private static void renderFlow(Vec3 diff, Direction face, PoseStack matrix, VertexConsumer bb, double s, double e, FluidStack fluid, FluidType type,
            int point, float partialTicks, int light) {
            double tickTime = mc.level.getGameTime();
            double offset = (tickTime + partialTicks) % 31 / 31.0;
            if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
                offset = -offset;
                face = face.getOpposite();
            }
            Vec3 dirVec = Vec3.atLowerCornerOf(face.getNormal());
            double ds = (point + 0.1) / 16.0;
            Vec3 vs = new Vec3(ds, ds, ds);
            Vec3 ve = new Vec3(1 - ds, 1 - ds, 1 - ds);
            diff = diff.subtract(dirVec.scale(offset));
            s += offset;
            e += offset;
            if (s < 0) {
                s++;
                e++;
                diff = diff.subtract(dirVec);
            }
            for (int i = 0; i <= e; i++) {
                Vec3 d = diff;
                diff = diff.add(dirVec);
                if (i < s - 1) {
                    continue;
                }
                matrix.translate(d.x, d.y, d.z);

                double s1 = s < i ? 0 : (s % 1);
                double e1 = e > i + 1 ? 1 : (e % 1);
                vs = VecUtil.replaceValue(vs, face.getAxis(), s1);
                ve = VecUtil.replaceValue(ve, face.getAxis(), e1);
                boolean[] sides = {true, true, true, true, true, true};
                if (s < i) {
                    sides[face.getOpposite().ordinal()] = false;
                }
                if (e > i + 1) {
                    sides[face.ordinal()] = false;
                }
                int blocklight0 = light&0x0000F0;
                int skylight0 = light&0xF00000;
                int blocklight = type.getLightLevel()<<4;
                blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
                int combinedLight = (skylight0)+(blocklight);
                FluidRenderer.vertex.lighti(combinedLight);
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, fluid, 1, 1, vs, ve, bb, matrix.last(), sides);
                matrix.translate(-d.x, -d.y, -d.z);
            }
        }
	
	

}
