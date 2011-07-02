package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;

public class BlockEngine extends BlockContainer {
	
	public BlockEngine(int i) {
		super(i, Material.wood);
		
		setLightValue(0.6F);

	}
	
	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public boolean func_28025_b () {
		return false;
	}

    public int getRenderType()
    {
    	return BuildCraftCore.blockByEntityModel;
    }

	@Override
	protected TileEntity getBlockEntity() {
		return new TileEngine();
	}
	
	 public void onBlockRemoval(World world, int i, int j, int k) {
		 ((TileEngine) world.getBlockTileEntity(i, j, k)).delete();
		 super.onBlockRemoval(world, i, j, k);
	 }
	 
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		((TileEngine) world.getBlockTileEntity(i, j, k)).switchOrientation();

		return false;
	}
	
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		tile.orientation = Orientations.YPos.ordinal();
		tile.switchOrientation();		
	}
	
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		tile.switchPower();
    }
    
	protected int damageDropped(int i) {
		return i;
	}
}
