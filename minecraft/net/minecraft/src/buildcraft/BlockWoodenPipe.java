package net.minecraft.src.buildcraft;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class BlockWoodenPipe extends BlockPipe {
	
	
	public BlockWoodenPipe(int i) {
		super(i, Material.wood);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/wooden_pipe.png");
	}	       
    
	@Override
	protected TileEntity getBlockEntity() {
		return new TileWoodenPipe ();
	}
	
    
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	TileWoodenPipe tile = (TileWoodenPipe) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileWoodenPipe();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
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
