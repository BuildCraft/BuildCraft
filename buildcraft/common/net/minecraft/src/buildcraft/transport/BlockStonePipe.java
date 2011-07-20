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
	
	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x2, y2, z2);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2);
		} else {
			return !(tile instanceof TileCobblestonePipe)
			&& super.isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2);
		}
	}
	
}
