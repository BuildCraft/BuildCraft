package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CompatHooks;

public class FacadeBlockAccess implements IBlockAccess {
	private final IBlockAccess world;
	private final ForgeDirection side;

	public FacadeBlockAccess(IBlockAccess world, ForgeDirection side) {
		this.world = world;
		this.side = side;
	}

	@Override
	public Block getBlock(int x, int y, int z) {
		Block compatBlock = CompatHooks.INSTANCE.getVisualBlock(world, x, y, z, side);
		if (compatBlock != null) {
			return compatBlock;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IPipeTile) {
			PipePluggable p = ((IPipeTile) tile).getPipePluggable(side);
			if (p instanceof IFacadePluggable) {
				return ((IFacadePluggable) p).getCurrentBlock();
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
		int compatMeta = CompatHooks.INSTANCE.getVisualMeta(world, x, y, z, side);
		if (compatMeta >= 0) {
			return compatMeta;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IPipeTile) {
			PipePluggable p = ((IPipeTile) tile).getPipePluggable(side);
			if (p instanceof IFacadePluggable) {
				//System.out.println("Found facade " + ((FacadePluggable) p).getRenderingMeta());
				return ((IFacadePluggable) p).getCurrentMetadata();
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
		return !(world.getTileEntity(x, y, z) instanceof IPipeTile);
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
