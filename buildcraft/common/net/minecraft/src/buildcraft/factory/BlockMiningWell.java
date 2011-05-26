package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockMiningWell extends BlockMachineRoot {
	
	public BlockMiningWell(int i) {
		super(i, Material.ground);
		
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		
		blockIndexInTexture = BuildCraftCore.transparentTexture;
	}
	
    public int getRenderType()
    {
        return BuildCraftCore.blockByEntityModel;
    }
	
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileMiningWell tile = (TileMiningWell) world.getBlockTileEntity(i, j, k);
    	
    	tile.dig();
    	
        return false;
    }
       
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {    	    	    	
    	TileMiningWell tile = (TileMiningWell) world.getBlockTileEntity(i, j, k);
    	
		tile.checkPower();
    }
    
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
		Orientations orientation = Utils.get2dOrientation(new Position(entityliving.posX,
				entityliving.posY, entityliving.posZ), new Position(i, j, k));    	
    	
    	world.setBlockMetadataWithNotify(i, j, k, orientation.reverse().ordinal()); 	
    }
    

	@Override
	protected TileEntity getBlockEntity() {		
		return new TileMiningWell();
	}
	
	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
    public void onBlockRemoval(World world, int i, int j, int k)
    {
    	TileMiningWell tile = (TileMiningWell) world.getBlockTileEntity(i, j, k);
    	tile.destroy();
    	
    	super.onBlockRemoval(world, i, j, k);
    }
	
}
