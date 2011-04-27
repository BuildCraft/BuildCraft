package net.minecraft.src.buildcraft;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;

public class BlockIronPipe extends BlockPipe {
	
	
	public BlockIronPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/iron_pipe.png");
	}
	
	public void onBlockPlaced(World world, int i, int j, int k, int l)
    {
		super.onBlockPlaced(world, i, j, j, l);
		
		world.setBlockMetadata(i, j, k, 1);
		moveOrientation (world, i, j, k);
    }
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		moveOrientation (world, i, j, k);
		world.markBlockNeedsUpdate(i, j, k);
		
		return false;
	}

	private void moveOrientation(World world, int i, int j, int k) {
		int metadata = world.getBlockMetadata(i, j, k);
		
		int nextMetadata = metadata;
		
		for (int l = 0; l < 6; ++l) {
			nextMetadata ++;
			
			if (nextMetadata > 5) {
				nextMetadata = 0;
			}
			
			Position pos = new Position(i, j, k,
					Orientations.values()[nextMetadata]);
			pos.moveForwards(1.0);
			
			TileEntity tile = world.getBlockTileEntity((int) pos.i,
					(int) pos.j, (int) pos.k);
			
			if (tile instanceof IPipeEntry || tile instanceof TileEntityChest) {
				world.setBlockMetadata(i, j, k, nextMetadata);
				return;
			}
		}
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileIronPipe ();
	}
	
}
