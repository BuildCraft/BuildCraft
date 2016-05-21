package buildcraft.core.lib.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Created by asie on 3/25/16.
 */
public class FakeBlockAccessSingleBlock implements IBlockAccess {
	private IBlockState state;

	public FakeBlockAccessSingleBlock(IBlockState state) {
		this.state = state;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return 0;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		if (pos.equals(BlockPos.ORIGIN)) {
			return state;
		} else {
			return Blocks.air.getDefaultState();
		}
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return !pos.equals(BlockPos.ORIGIN);
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
		return null;
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.DEBUG_WORLD;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
	}
}
