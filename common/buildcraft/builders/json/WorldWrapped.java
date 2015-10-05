package buildcraft.builders.json;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;

public class WorldWrapped extends World {
	private final World wrapped;
	private final int targetX, targetY, targetZ;
	private final TileEntity targetTE;

	public WorldWrapped(World wrapped, int x, int y, int z, TileEntity targetTE) {
		super(wrapped.getSaveHandler(), wrapped.getWorldInfo().getWorldName(), wrapped.provider, new WorldSettings(wrapped.getWorldInfo()), wrapped.theProfiler);
		this.wrapped = wrapped;
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
		this.targetTE = targetTE;
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		if (x == targetX && y == targetY && z == targetZ) {
			return targetTE;
		} else {
			return super.getTileEntity(x, y, z);
		}
	}

	// Minor safeguards

	@Override
	public boolean setBlock(int x, int y, int z, Block block, int meta, int flag) {
		return false;
	}

	@Override
	public boolean setBlockToAir(int x, int y, int z) {
		return false;
	}

	@Override
	public boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flag) {
		markAndNotifyBlock(x, y, z, getChunkFromBlockCoords(x, z), getBlock(x, y, z), getBlock(x, y, z), flag);
		return false;
	}

	@Override
	public void setTileEntity(int x, int y, int z, TileEntity tileEntity) {

	}

	// Required to implement

	@Override
	protected IChunkProvider createChunkProvider() {
		return wrapped.getChunkProvider();
	}

	@Override
	protected int func_152379_p() {
		return 0;
	}

	@Override
	public Entity getEntityByID(int id) {
		return wrapped.getEntityByID(id);
	}
}
