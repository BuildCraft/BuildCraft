package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.FillerPattern;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockFiller extends BlockContainer implements ITextureProvider {

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
			
		if (APIProxy.getWorld() == null) {
			return getBlockTextureFromSideAndMetadata(i, m);
		}
		
		TileEntity tile = APIProxy.getWorld().getBlockTileEntity(
				i, j, k);				
		
		if (tile != null && tile instanceof TileFiller) {
			TileFiller filler = (TileFiller) tile;
			if (l == 1 || l == 0) {
				if (filler.done) {
					return textureTopOff;
				} else {
					return textureTopOn;
				}
			} else if (filler.currentPattern != null) {
				return filler.currentPattern.getTextureIndex();
			} else {
				return textureSides;
			}
		}

    	return getBlockTextureFromSideAndMetadata(l, m);
	}	

    public int getBlockTextureFromSide(int i) {
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
	
	public void onBlockRemoval(World world, int i, int j, int k) {		
		Utils.preDestroyBlock(world, i, j, k);
		
		super.onBlockRemoval(world, i, j, k);
	}

    @Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
}
