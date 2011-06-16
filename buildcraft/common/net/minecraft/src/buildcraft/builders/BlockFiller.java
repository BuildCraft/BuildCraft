package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.FillerPattern;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;

public class BlockFiller extends BlockContainer implements ICustomTextureBlock {

	int textureSides;
	int textureTopOn;
	int textureTopOff;
	public FillerPattern currentPattern;
	
	public BlockFiller(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
		
		textureSides = 4 * 16 + 2;
		textureTopOn = 4 * 16 + 0;
		textureTopOff = 4 * 16 + 1;
	}
	
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		
		TileFiller tile = (TileFiller) world.getBlockTileEntity(i, j, k);	
		BuildersProxy.displayGUIFiller(entityplayer, tile);
		
		return true;
	}
	
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		int m = iblockaccess.getBlockMetadata(i, j, k);
			
		TileFiller tile = (TileFiller) iblockaccess.getBlockTileEntity(i, j, k);
		
		if (tile != null) {
			if (l == 1 || l == 0) {
				if (tile.done) {
					return textureTopOff;
				} else {
					return textureTopOn;
				}
			} else if (tile.currentPattern != null) {
				return tile.currentPattern.getTextureIndex();
			} else {
				return textureSides;
			}
		}

    	return getBlockTextureFromSideAndMetadata(l, m);
	}	

    public int getBlockTextureFromSide(int i)
    {
        if (i == 0 || i == 1) {
        	return textureTopOn;
        } else {
        	return textureSides;
        }
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

    @Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
}
