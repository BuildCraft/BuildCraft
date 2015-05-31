package buildcraft.core.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;
import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.core.PropertyDouble;

/** This class is designed to be called from any instance of BlockFluidBase, not just Buildcraft's version. It is
 * recommended that you override the getExtendedState() method in your block and just return
 * FluidHelper.getExtendedState()
 * 
 * Original implementation by kirderf1 () */
public class FluidHelper {
	public static final PropertyDouble HEIGHT_NE = BuildCraftProperties.FLUID_HEIGHT_NE;
	public static final PropertyDouble HEIGHT_NW = BuildCraftProperties.FLUID_HEIGHT_NW;
	public static final PropertyDouble HEIGHT_SE = BuildCraftProperties.FLUID_HEIGHT_SE;
	public static final PropertyDouble HEIGHT_SW = BuildCraftProperties.FLUID_HEIGHT_SW;
	public static final PropertyDouble FLOW_DIRECTION = BuildCraftProperties.FLUID_FLOW_DIRECTION;
	public static final PropertyInteger LEVEL = BlockFluidBase.LEVEL;

	private FluidHelper() {}

	public static ExtendedBlockState createBlockState(Block block) {
		return new ExtendedBlockState(block, new IProperty[] { LEVEL }, new IUnlistedProperty[] { HEIGHT_NE, HEIGHT_NW, HEIGHT_SE, HEIGHT_SW,
			FLOW_DIRECTION });
	}

	public static IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		BlockFluidBase fluid = (BlockFluidBase) state.getBlock();

		double heightNW, heightSW, heightSE, heightNE;
		double flow11 = getFluidHeightForRender(world, pos, fluid);

		if (flow11 != 1) {
			double flow00 = getFluidHeightForRender(world, pos.add(-1, 0, -1), fluid);
			double flow01 = getFluidHeightForRender(world, pos.add(-1, 0, 0), fluid);
			double flow02 = getFluidHeightForRender(world, pos.add(-1, 0, 1), fluid);
			double flow10 = getFluidHeightForRender(world, pos.add(0, 0, -1), fluid);
			double flow12 = getFluidHeightForRender(world, pos.add(0, 0, 1), fluid);
			double flow20 = getFluidHeightForRender(world, pos.add(1, 0, -1), fluid);
			double flow21 = getFluidHeightForRender(world, pos.add(1, 0, 0), fluid);
			double flow22 = getFluidHeightForRender(world, pos.add(1, 0, 1), fluid);

			heightNW = getFluidHeightAverage(new double[] { flow00, flow01, flow10, flow11 });
			heightSW = getFluidHeightAverage(new double[] { flow01, flow02, flow12, flow11 });
			heightSE = getFluidHeightAverage(new double[] { flow12, flow21, flow22, flow11 });
			heightNE = getFluidHeightAverage(new double[] { flow10, flow20, flow21, flow11 });
		}
		else {
			heightNW = flow11;
			heightSW = flow11;
			heightSE = flow11;
			heightNE = flow11;
		}

		IExtendedBlockState extState = (IExtendedBlockState) state;
		extState = extState.withProperty(HEIGHT_NW, heightNW).withProperty(HEIGHT_SW, heightSW);
		extState = extState.withProperty(HEIGHT_SE, heightSE).withProperty(HEIGHT_NE, heightNE);
		extState = extState.withProperty(FLOW_DIRECTION, BlockFluidBase.getFlowDirection(world, pos));

		return extState;
	}

	public static double getFluidHeightForRender(IBlockAccess world, BlockPos pos, BlockFluidBase fluid) {
		IBlockState state = world.getBlockState(pos);
		int density = BlockFluidBase.getDensity(world, pos);
		Block verticalOrigin = world.getBlockState(pos.down(density > 0 ? 1 : -1)).getBlock();
		if (state.getBlock() == fluid) {
			if (verticalOrigin.getMaterial().isLiquid() || verticalOrigin instanceof IFluidBlock) {
				return 1;
			}

			if ((Integer) state.getValue(LEVEL) == fluid.getMaxRenderHeightMeta()) {
				return 0.875F;
			}
		}
		return !state.getBlock().getMaterial().isSolid() && verticalOrigin == fluid ? 1 : fluid.getQuantaPercentage(world, pos) * 0.875F;
	}

	public static double getFluidHeightAverage(double[] height) {
		double total = 0;
		int count = 0;
		double end = 0;

		for (int i = 0; i < height.length; i++) {
			if (height[i] >= 0.875F && end != 1F) {
				end = height[i];
			}

			if (height[i] >= 0) {
				total += height[i];
				count++;
			}
		}

		if (end == 0)
			end = total / count;

		return end;
	}
}
