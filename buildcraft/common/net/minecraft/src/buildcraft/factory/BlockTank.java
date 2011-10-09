/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.forge.ITextureProvider;

public class BlockTank extends BlockContainer implements ITextureProvider {

	public BlockTank(int i) {
		super(i, Material.glass);
		
		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
		setHardness(1F);
		
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isOpaqueCube() {
		return false;
	}
    
	public boolean isACube () {
    	return false;
    }    
    
	@Override
	public TileEntity getBlockEntity() {
		return new TileTank ();
	}

	@Override
	public String getTextureFile() {		
		return BuildCraftCore.customBuildCraftTexture;
	}

	 public int getBlockTextureFromSide(int i) {
		 switch (i) {
		 case 0: case 1:
			 return 6 * 16 + 2;		 
		 default:
			 return 6 * 16 + 0;		 
		 }
	 }
	 
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k,
			int l) {
		switch (l) {
		case 0: case 1:
			return 6 * 16 + 2;
		default:
			if (iblockaccess.getBlockId(i, j - 1, k) == blockID) {
				return 6 * 16 + 1;
			} else {
				return 6 * 16 + 0;
			}
		}
	}
	
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		
		if (entityplayer.getCurrentEquippedItem() != null) {
			int itemId = entityplayer.getCurrentEquippedItem().itemID;
			int liquidId = API.getLiquidForBucket(itemId);
			
			TileTank tank = (TileTank) world.getBlockTileEntity(i, j, k);

			if (liquidId != 0) {
				int qty = tank.fill(
						Orientations.Unknown, BuildCraftCore.BUCKET_VOLUME,
						liquidId, true);

				if (qty != 0 && !BuildCraftCore.debugMode) {
					entityplayer.inventory.setInventorySlotContents(
							entityplayer.inventory.currentItem,
							new ItemStack(Item.bucketEmpty, 1));										
				}
				
				return true;
			}
		}
		
		return false;
	}
	
}
