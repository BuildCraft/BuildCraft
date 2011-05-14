package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockWoodenPipe extends BlockPipe {
	
	
	public BlockWoodenPipe(int i) {
		super(i, Material.wood);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/transport/gui/wooden_pipe.png");
	}	       
    
	@Override
	protected TileEntity getBlockEntity() {
		return new TileWoodenPipe ();
	}
	
    
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i, j, k);
    	
    	tile.extract();
    	
        return false;
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileWoodenPipe();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.checkPower();
    }
	
}
