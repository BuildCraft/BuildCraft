package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockQuarry extends BlockMachineRoot implements
		ICustomTextureBlock {
	
	int textureTop;
	int textureFront;
	int textureSide;
	
	public BlockQuarry(int i) {
		super(i, Material.iron);
		
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		
		textureSide = 2 * 16 + 9;
		textureFront = 2 * 16 + 7;
		textureTop = 2 * 16 + 8;	
		
	}
    
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
    	super.onBlockPlacedBy(world, i, j, k, entityliving);
    	
		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));
    	
		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {    	    	    	
    	TileQuarry tile = (TileQuarry) world.getBlockTileEntity(i, j, k);
    	    	
		tile.checkPower();    	        
    }

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	TileQuarry tile = (TileQuarry) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileQuarry();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.tryWork();
    	
        return false;
    }

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}
		
		if (i == j) {
			return textureFront;
		}

		switch (i) {
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}
    
	@Override
	protected TileEntity getBlockEntity() {		
		return new TileQuarry();
	}
	

	public void onBlockRemoval(World world, int i, int j, int k) {
		((TileQuarry) world.getBlockTileEntity(i, j, k)).delete();
		
		super.onBlockRemoval(world, i, j, k);
	}
	
	@Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
}
