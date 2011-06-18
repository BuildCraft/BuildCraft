package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;

public class BlockStonePipe extends BlockPipe {
	
	
	public BlockStonePipe(int i) {
		super(i, Material.rock);

		blockIndexInTexture = 1 * 16 + 13;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileStonePipe ();
	}
	
	public boolean isPipeConnected(IBlockAccess blockAccess, int x, int y, int z) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x, y, z);
		} else {
			return !(tile instanceof TileCobblestonePipe)
			&& super.isPipeConnected(blockAccess, x, y, z);
		}
	}
	
}
