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
//		
//		float f = world.rand.nextFloat() * 0.8F + 0.1F;
//		float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
//		float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
//
//		int bptNumber = tile.getBluePrintNumber ();
//		
//		if (bptNumber != -1) {
//			EntityItem entityitem = new EntityItem(world, (float) i + f, (float) j
//					+ f1 + 0.5F, (float) k + f2, new ItemStack(
//							mod_BuildCraftBuilders.templateItem, 1, bptNumber));
//			
//			CoreProxy.addName(entityitem.item, "Template #" + bptNumber);
//
//			float f3 = 0.05F;
//			entityitem.motionX = (float) world.rand.nextGaussian() * f3;
//			entityitem.motionY = (float) world.rand.nextGaussian() * f3;
//			entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
//			world.entityJoinedWorld(entityitem);
//		}
//		
//		return true;		
	}	
	
	public void onBlockRemoval(World world, int i, int j, int k) {		
		((TileTemplate) world.getBlockTileEntity(i, j, k)).destroy();
		
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
