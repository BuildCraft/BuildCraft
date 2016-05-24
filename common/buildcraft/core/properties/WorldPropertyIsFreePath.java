package buildcraft.core.properties;

import buildcraft.api.core.BuildCraftAPI;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WorldPropertyIsFreePath extends WorldProperty {

	@Override
	public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		Block block = state.getBlock();
		return block == null || block.isAir(blockAccess, pos) || BuildCraftAPI.softBlocks.contains(block) || block.isReplaceable((World) blockAccess, pos)
				|| hasNoCollisionBoundingBox(blockAccess, state, pos);
	}

	private static boolean hasNoCollisionBoundingBox(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		if (blockAccess instanceof World) {
			// Somewhat bad that we don't always have a World instance
			return state.getBlock().getCollisionBoundingBox((World) blockAccess, pos, state) == null;
		}
		try {
			// Do this or just return null?
			// This could cause unexpected behavior
			return state.getBlock().getCollisionBoundingBox(null, pos, state) == null;
		} catch (NullPointerException e) {
			// Expected, since worldIn is null
		} catch (Exception e) {
			// Better catch all the Exception, i have no idea what worldIn=null
			// could cause
		}
		return false;
	}

}
