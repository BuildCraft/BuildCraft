package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockTemplate extends BlockContainer implements ICustomTextureBlock {

	int blockTextureSides;
	int blockTextureFront;
	int blockTextureTopPos;
	int blockTextureTopNeg;
	
	
	public BlockTemplate(int i) {
		super(i, Material.iron);
		setHardness(0.5F);
		blockTextureSides = 3 * 16 + 0;
		blockTextureTopNeg = 3 * 16 + 1;
		blockTextureTopPos = 3 * 16 + 2;
		blockTextureFront = 3 * 16 + 4;
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
    public int getRenderType() {
        return BuildCraftCore.customTextureModel;
    }

	@Override
	protected TileEntity getBlockEntity() {
		return new TileTemplate();
	}
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {		
		TileTemplate tile = (TileTemplate) world.getBlockTileEntity(i, j, k);				
		BuildersProxy.displayGUITemplate(entityplayer, tile);
		
		return true;	
	}	
	
	public void onBlockRemoval(World world, int i, int j, int k) {		
		Utils.preDestroyBlock(world, i, j, k);
		
		super.onBlockRemoval(world, i, j, k);
	}
	
	public void onBlockPlacedBy(World world, int i, int j, int k,
			EntityLiving entityliving) {
		super.onBlockPlacedBy(world, i, j, k, entityliving);

		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
	}
	
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		int m = iblockaccess.getBlockMetadata(i, j, k);
					    	
    	if (l == 1) {
    		boolean isPowered = false;
    		
			isPowered = APIProxy.getWorld().getBlockTileEntity(i, j, k).worldObj
					.isBlockIndirectlyGettingPowered(i, j, k);
    		
    		if (!isPowered) {
    			return blockTextureTopPos;
    		} else {
    			return blockTextureTopNeg;
    		}
    	}

    	return getBlockTextureFromSideAndMetadata(l, m);
	}	
	
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
    	if (j == 0 && i == 3) {
			return blockTextureFront;
		}
    	
    	if (i == 1) {
    		return blockTextureTopPos;
    	}
		
		if (i == j) {
			return blockTextureFront;
		}
		
		return blockTextureSides;		
    }
}
