package buildcraft.builders.json;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldWrapped extends World {
	private final World wrapped;
	private final BlockPos targetPos;
	private final TileEntity targetTE;

	public WorldWrapped(World wrapped, BlockPos pos, TileEntity targetTE) {
		super(wrapped.getSaveHandler(), wrapped.getWorldInfo(), wrapped.provider, wrapped.theProfiler, false);
		this.wrapped = wrapped;
		this.targetPos = pos;
		this.targetTE = targetTE;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		if (targetPos.equals(pos)) {
			return targetTE;
		} else {
			return super.getTileEntity(pos);
		}
	}

	// Minor safeguards

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState state, int flag) {
		return false;
	}

	@Override
	public boolean setBlockToAir(BlockPos pos) {
		return false;
	}

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState state) {
		return false;
	}

	@Override
	public void setTileEntity(BlockPos pos, TileEntity tileEntity) {

	}

	// Required to implement

	@Override
	protected int getRenderDistanceChunks() {
		return 1;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return wrapped.getChunkProvider();
	}

	@Override
	public Entity getEntityByID(int id) {
		return wrapped.getEntityByID(id);
	}
}
