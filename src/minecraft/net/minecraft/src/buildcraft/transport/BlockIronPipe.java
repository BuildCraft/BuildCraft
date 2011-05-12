package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockIronPipe extends BlockPipe {
	
	
	public BlockIronPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/transport/gui/iron_pipe.png");
	}
	
	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l)
    {		
		super.onBlockPlaced(world, i, j, j, l);
		
		TileIronPipe tile = Utils.getSafeTile(world, i, j, k,
				TileIronPipe.class);
		
		if (tile == null) {
			tile = new TileIronPipe();
			world.setBlockTileEntity(i, j, k, tile);
		}
		
		world.setBlockMetadata(i, j, k, 1);
		tile.moveOrientation ();
    }
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileIronPipe tile = Utils.getSafeTile(world, i, j, k,
				TileIronPipe.class);
		
		if (tile == null) {
			tile = new TileIronPipe();
			world.setBlockTileEntity(i, j, k, tile);
		}
		
		tile.moveOrientation ();
		world.markBlockNeedsUpdate(i, j, k);
		
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		TileIronPipe tile = Utils.getSafeTile(world, i, j, k,
				TileIronPipe.class);

		if (tile == null) {
			tile = new TileIronPipe();
			world.setBlockTileEntity(i, j, k, tile);
		}

		tile.checkPower();
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileIronPipe ();
	}
	
}
