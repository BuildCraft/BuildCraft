package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockFiller extends BlockContainer {

	public BlockFiller(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
		
		blockIndexInTexture = 3 * 16 + 0;
	}
	
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		
		entityplayer.displayGUIChest(((IInventory) (world.getBlockTileEntity(i,
				j, k))));
		
		return true;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileFiller();
	}
	

    public void onNeighborBlockChange(World world, int i, int j, int k, int l)
    {
    	((TileFiller) world.getBlockTileEntity(i, j, k)).checkPower();
    }
	
	public void onBlockRemoval(World world, int i, int j, int k) {
		
		((TileFiller) world.getBlockTileEntity(i, j, k)).destroy();
		
		super.onBlockRemoval(world, i, j, k);
	}
	
    public int getRenderType() {
        return BuildCraftCore.customTextureModel;
    }


}
