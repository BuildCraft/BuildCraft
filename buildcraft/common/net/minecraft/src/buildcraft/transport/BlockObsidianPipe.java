package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Entity;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;

public class BlockObsidianPipe extends BlockPipe {
	
	public BlockObsidianPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = 1 * 16 + 12;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileObsidianPipe ();
	}
	
    public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {    		
    	if (entity.isDead) {
    		return;
    	}
    	
		TileObsidianPipe tile = (TileObsidianPipe)world.getBlockTileEntity(i, j, k);
		
		if (tile.canSuck(entity, 0)) {
			tile.pullItemIntoPipe(entity, 0);
		}
    }
    
    @Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x2, y2, z2);

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2);
		} else {
			return !(tile instanceof TileObsidianPipe)
			&& super.isPipeConnected(blockAccess, x1, y1, z1, x2, y2, z2);
		}
	}
}
