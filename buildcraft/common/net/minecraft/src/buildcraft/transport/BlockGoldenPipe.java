package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;

public class BlockGoldenPipe extends BlockPipe {
	
	
	private int inactiveTexture;
	private int activeTexture;

	public BlockGoldenPipe(int i) {
		super(i, Material.iron);

		inactiveTexture = 1 * 16 + 4;
		activeTexture = 1 * 16 + 14;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileGoldenPipe ();
	}
	
    public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
    	boolean isPowered = false;
		
		if (APIProxy.getWorld() == null) {
			return getBlockTextureFromSideAndMetadata(i,
					iblockaccess.getBlockMetadata(i, j, k));
		}
    	
		isPowered = APIProxy.getWorld()
				.isBlockIndirectlyGettingPowered(i, j, k);
		
		if (isPowered) {
			return activeTexture;
		} else {
			return inactiveTexture;
		}
    }

    public int getBlockTextureFromSideAndMetadata(int i, int j)
    {
        return inactiveTexture;
    }

	public boolean isPipeConnected(IBlockAccess blockAccess, int x, int y, int z) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x, y, z);
		} else {
			return !(tile instanceof TileGoldenPipe)
			&& super.isPipeConnected(blockAccess, x, y, z);
		}
	}
}
