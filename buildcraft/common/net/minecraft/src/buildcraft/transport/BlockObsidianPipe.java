package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

import net.minecraft.src.Entity;
import net.minecraft.src.World;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityItem;

public class BlockObsidianPipe extends BlockPipe {
	
	public BlockObsidianPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = 1 * 16 + 12;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileObsidianPipe ();
	}
	
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {	
    	TileObsidianPipe tile = (TileObsidianPipe) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileObsidianPipe();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	tile.tryWork();
    	
        return false;
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	TileObsidianPipe tile = (TileObsidianPipe) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileObsidianPipe();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.checkPower();
    }
	
    public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
    {
		TileObsidianPipe tile = (TileObsidianPipe)world.getBlockTileEntity(i, j, k);
		if (tile == null) {
    		tile = new TileObsidianPipe();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
		if(entity instanceof EntityItem)
		{
			tile.pullItemIntoPipe((EntityItem)entity);					
		}
    }
}
