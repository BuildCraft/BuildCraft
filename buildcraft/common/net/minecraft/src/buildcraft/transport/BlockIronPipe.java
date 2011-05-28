package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockIronPipe extends BlockPipe {
	
	
	public BlockIronPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = 1 * 16 + 2;
	}
	
	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l)
    {		
		super.onBlockPlaced(world, i, j, j, l);
		
		TileIronPipe tile = (TileIronPipe) world.getBlockTileEntity(i, j, k);
		world.setBlockMetadata(i, j, k, 1);
		tile.tryWork();
    }
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileIronPipe tile = (TileIronPipe) world.getBlockTileEntity(i, j, k);
		
		tile.tryWork();
		world.markBlockNeedsUpdate(i, j, k);
		
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		TileIronPipe tile = (TileIronPipe) world.getBlockTileEntity(i, j, k);

		tile.checkPower();
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileIronPipe ();
	}
	
}
