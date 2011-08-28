package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
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
		if (entityplayer.getCurrentEquippedItem() != null 
				&& entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i,
					j, k);

			tile.switchSource();

			return true;
		}
    	
        return false;
    }
        
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	super.onNeighborBlockChange(world, i, j, k, l);
    	
    	TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i, j, k);
    	tile.scheduleNeighborChange();   	
    }
	
    public int getTextureForConnection (Orientations connection, int metadata) {
    	if (metadata == connection.ordinal()) {
			return plainWoodenPipeTexture;
    	} else {
    		return blockIndexInTexture;
    	}
				
    }
	
    @Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x2, y2, z2);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2);
		} else {
			return !(tile instanceof TileWoodenPipe)
			&& super.isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2);
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
