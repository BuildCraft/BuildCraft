package ct.buildcraft.factory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import ct.buildcraft.factory.BCFactorySprites;
import ct.buildcraft.factory.block.BlockDistiller;
import ct.buildcraft.factory.tile.TileDistiller_BC8;
import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.client.render.fluid.FluidSpriteType;
import ct.buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class $RenderDistiller implements BlockEntityRenderer<TileDistiller_BC8> {

	public static final ResourceLocation DISTILLER_POWER_A = BCFactorySprites.DISTILLER_POWER_A;
	public static final ResourceLocation DISTILLER_POWER_B = BCFactorySprites.DISTILLER_POWER_B;
	public static final ResourceLocation DISTILLER_POWER_C = BCFactorySprites.DISTILLER_POWER_C;
	public static final ResourceLocation DISTILLER_POWER_D = BCFactorySprites.DISTILLER_POWER_D;

	protected static final Vec3 INPUT_BOX_MIN = new Vec3(0.01, 0, 0.25+0.01);
	protected static final Vec3 INPUT_BOX_MAX = new Vec3(0.5-0.01, 1-0.01, 0.75-0.01);
	protected static final Vec3 OUTPUT_BOX_MIN = new Vec3(0.5+0.01, 0, 0+0.01);
	protected static final Vec3 OUTPUT_BOX_MAX = new Vec3(1-0.01, 0.5-0.01, 1-0.01);
	protected static final boolean[] forRender = { true, true, true, true, true, true };
	
	public $RenderDistiller(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(TileDistiller_BC8 tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light,
			int overlay) {
		FluidStackInterp tankIn = tile.smoothedTankIn.getFluidForRender(partialTicks);
		FluidStackInterp tankgas = tile.smoothedTankGasOut.getFluidForRender(partialTicks);
		FluidStackInterp tankFluid = tile.smoothedTankLiquidOut.getFluidForRender(partialTicks);
		matrix.pushPose();
		VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
		matrix.translate(0.5, 0.5, 0.5);
//		matrix.mulPose(tile.getBlockState().getValue(BlockDistiller.PROP_FACING).getRotation());
        switch(tile.getBlockState().getValue(BlockDistiller.PROP_FACING)) {
		case EAST:
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));
			break;
		case NORTH:
			matrix.mulPose(Vector3f.YN.rotationDegrees(90));
			break;
		case SOUTH:
			matrix.mulPose(Vector3f.YP.rotationDegrees(90));
			break;
		case WEST:
			break;
		default:
			break;
        }
		matrix.translate(-0.5, -0.5, -0.5);
		int blocklight0 = light & 0x0000F0;
		int skylight0 = light & 0xF00000;
		if (tankIn != null) {
			FluidStack tankInFluid = tankIn.fluid;
			int blocklight = tankInFluid.getFluid().getFluidType().getLightLevel(tankInFluid) << 4;
			blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
			int combinedLight = (skylight0) + (blocklight);
			FluidRenderer.vertex.lighti(combinedLight);
			FluidRenderer.renderFluid(FluidSpriteType.STILL, tankInFluid, tankIn.amount, 4000, INPUT_BOX_MIN, INPUT_BOX_MAX, bb, matrix.last(), forRender);
		}
		if(tankFluid != null) {
			FluidStack tankFluidFluid = tankFluid.fluid;
			int blocklight = tankFluidFluid.getFluid().getFluidType().getLightLevel(tankFluidFluid) << 4;
			blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
			int combinedLight = (skylight0) + (blocklight);
			FluidRenderer.vertex.lighti(combinedLight);
			FluidRenderer.renderFluid(FluidSpriteType.STILL, tankFluidFluid, tankFluid.amount, 4000, OUTPUT_BOX_MIN, OUTPUT_BOX_MAX, bb, matrix.last(), forRender);
		}
		matrix.translate(0.5, 0.5, 0.5);
		matrix.mulPose(Vector3f.XP.rotationDegrees(180));
		matrix.translate(-0.5, -0.5+0.005, -0.5);
		if(tankgas != null) {
			FluidStack tankgasFluid = tankgas.fluid;
			int blocklight = tankgasFluid.getFluid().getFluidType().getLightLevel(tankgasFluid) << 4;
			blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
			int combinedLight = (skylight0) + (blocklight);
			FluidRenderer.vertex.lighti(combinedLight);
			FluidRenderer.renderFluid(FluidSpriteType.STILL, tankgasFluid, tankgas.amount, 4000, OUTPUT_BOX_MIN, OUTPUT_BOX_MAX, bb, matrix.last(), forRender);
		}
		matrix.popPose();
	}

}
