package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Entity;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;

public class BlockObsidianPipe extends BlockPipe{
	
	public BlockObsidianPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = 1 * 16 + 12;
	}
	
	@Override
	public TileEntity getBlockEntity() {
		return new TileObsidianPipe ();
	}
	
    public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {    		
    	if (entity.isDead) {
    		return;
    	}
    	
		TileObsidianPipe tile = (TileObsidianPipe)world.getBlockTileEntity(i, j, k);
		
		if (tile.canSuck(entity)) {
			tile.pullItemIntoPipe(entity);
		}
    }
    
	public boolean isPipeConnected(IBlockAccess blockAccess, int x, int y, int z) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x, y, z);
		} else {
			return !(tile instanceof TileObsidianPipe)
			&& super.isPipeConnected(blockAccess, x, y, z);
		}
	}
}
