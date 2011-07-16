package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
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

public class BlockBuilder extends BlockContainer implements ICustomTextureBlock {

	int blockTextureTop;
	int blockTextureSide;
	int blockTextureFront;
	
	public BlockBuilder(int i) {
		super(i, Material.iron);
		blockTextureSide = 3 * 16 + 5;
		blockTextureTop = 3 * 16 + 6;
		blockTextureFront = 3 * 16 + 7;
		setHardness(0.7F);
	}

	@Override
	protected TileEntity getBlockEntity() {
		return new TileBuilder();
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
    public int getRenderType() {
        return BuildCraftCore.customTextureModel;
    }

    public int getBlockTextureFromSideAndMetadata(int i, int j) {
    	if (j == 0 && i == 3) {
			return blockTextureFront;
		}
		
		if (i == j) {
			return blockTextureFront;
		}

		switch (i) {
		case 1:
			return blockTextureTop;
		default:
			return blockTextureSide;
		}
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	((TileBuilder) world.getBlockTileEntity(i, j, k)).checkPower();
    }
    
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {		
		TileBuilder tile = (TileBuilder) world.getBlockTileEntity(i, j, k);				
		BuildersProxy.displayGUIBuilder(entityplayer, tile);
		
		return true;
	}
	
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
    	super.onBlockPlacedBy(world, i, j, k, entityliving);
    	
		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));
    	
		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
    }
    

    public void onBlockRemoval(World world, int i, int j, int k) {
    	Utils.preDestroyBlock(world, i, j, k);
    	super.onBlockRemoval(world, i, j, k);
    }

}
