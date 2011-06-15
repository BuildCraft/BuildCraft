package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.ChunkCache;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

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
	
    public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l)
    {
    	boolean isPowered = false;
		
		if (iblockaccess instanceof ChunkCache) {
			isPowered = ((ChunkCache) iblockaccess).getBlockTileEntity(i, j, k).worldObj
					.isBlockIndirectlyGettingPowered(i, j, k);
		}
		
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

	
}
