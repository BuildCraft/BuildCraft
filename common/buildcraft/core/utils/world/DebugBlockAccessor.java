package buildcraft.core.utils.world;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

/** This is an infinite fake world, which will pretend to have all the different block states for rendering blocks in
 * pipes etc */
public class DebugBlockAccessor implements IBlockAccess {
	private final List<IBlockState> validStates;
	// Holding variables for the world, so they
	public IBlockAccess world;
	public BlockPos currentPos;

	private final BiomeGenBase defaultBiomeGen = BiomeGenBase.plains;

	public DebugBlockAccessor(List<IBlockState> states) {
		validStates = states;
	}

	/** There are no tile entities -we cannot assume that all tile entities would be safe for holding in a non-ticking
	 * world, while being able to render at the same time */
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	/** All light values are their maximum */
	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return 15;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		if (pos.getY() != 70)
			return Blocks.air.getDefaultState();
		int i = pos.getX();
		i %= validStates.size();
		return validStates.get(i);
	}

	public BlockPos getBlockPos(IBlockState state) {
		return new BlockPos(validStates.indexOf(state), 70, 0);
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return pos.getY() != 70;
	}

	public void setTempWorld(IBlockAccess blockAccess, BlockPos pos) {
		world = blockAccess;
		currentPos = pos;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
		if (world != null && currentPos != null) {
			BiomeGenBase biomeGenBase = world.getBiomeGenForCoords(currentPos);
			if (biomeGenBase != null)
				return biomeGenBase;
		}
		return defaultBiomeGen;
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	/** No redstone in this world. */
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
		return false;
	}
}
