package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.FacadePluggable;
import buildcraft.transport.TileGenericPipe;

public class FacadeBlockAccess implements IBlockAccess {
	private final IBlockAccess world;
	private final ForgeDirection side;

	public FacadeBlockAccess(IBlockAccess world, ForgeDirection side) {
		this.world = world;
		this.side = side;
	}

	@Override
	public Block getBlock(int x, int y, int z) {
		System.out.println("Querying block at " + x + ", " + y + ", " + z);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileGenericPipe) {
			PipePluggable p = ((TileGenericPipe) tile).getPipePluggable(side);
			if (p instanceof FacadePluggable) {
				System.out.println("Found facade");
				return ((FacadePluggable) p).getRenderingBlock();
			}
		}
		return Blocks.air;
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		return null;
	}

	@Override
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int a) {
		return 0;
	}

	@Override
	public int getBlockMetadata(int x, int y, int z) {
		System.out.println("Querying block metadata at " + x + ", " + y + ", " + z);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileGenericPipe) {
			PipePluggable p = ((TileGenericPipe) tile).getPipePluggable(side);
			if (p instanceof FacadePluggable) {
				System.out.println("Found facade " + ((FacadePluggable) p).getRenderingMeta());
				return ((FacadePluggable) p).getRenderingMeta();
			}
		}
		return 0;
	}

	@Override
	public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
		return 0;
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		return !(world.getBlock(x, y, z) instanceof BlockGenericPipe);
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return world.getBiomeGenForCoords(x, z);
	}

	@Override
	public int getHeight() {
		return world.getHeight();
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return world.extendedLevelsInChunkCache();
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean def) {
		return world.isSideSolid(x, y, z, side, def);
	}
}
