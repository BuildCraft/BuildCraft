package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockDiamondPipe extends BlockPipe {
	
	
	public BlockDiamondPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = 1 * 16 + 5;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileDiamondPipe ();
	}

	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {		
		TileDiamondPipe	tileRooter = (TileDiamondPipe) world.getBlockTileEntity(i, j, k);
		
		TransportProxy.displayGUIFilter(entityplayer, tileRooter);
		
		return true;
	}	    
}
