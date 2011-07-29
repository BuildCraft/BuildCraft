package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.APIProxy;

public class BlockWoodenPipe extends BlockPipe {
	
	public static String [] excludedBlocks;
	
	int plainWoodenPipeTexture;
	
	public BlockWoodenPipe(int i) {
		super(i, Material.wood);

		blockIndexInTexture = 1 * 16 + 0;
		plainWoodenPipeTexture = 1 * 16 + 15;
	}	       
    
	@Override
	protected TileEntity getBlockEntity() {
		return new TileWoodenPipe ();
	}
	
    
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i, j, k);
    	
    	tile.switchSource();
    	
        return false;
    }
        
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i, j, k);
    	tile.scheduleSourceSet();    	
    }
	
    public int getTextureForConnection (Orientations connection, int metadata) {
    	if (metadata == connection.ordinal()) {
			return plainWoodenPipeTexture;
    	} else {
    		return blockIndexInTexture;
    	}
				
    }
	
	public boolean isPipeConnected(IBlockAccess blockAccess, int x, int y, int z) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x, y, z);
		} else {
			return !(tile instanceof TileWoodenPipe)
			&& super.isPipeConnected(blockAccess, x, y, z);
		}
	}
	
	public static boolean isExcludedFromExtraction (Block block) {
		if (block == null) {
			return true;
		}
		
		for (String excluded : excludedBlocks) {			
			if (excluded.equals (block.getBlockName())
					|| excluded.equals (Integer.toString(block.blockID))) {
				return true;
			}
		}
		
		return false;
	}
}
