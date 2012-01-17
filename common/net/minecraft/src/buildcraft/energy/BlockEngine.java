/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.IItemPipe;

public class BlockEngine extends BlockContainer {
	
	public BlockEngine(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
    public int getRenderType()
    {
    	return BuildCraftCore.blockByEntityModel;
    }

	@Override
	public TileEntity getBlockEntity() {
		return new TileEngine();
	}

	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		TileEngine engine = ((TileEngine) world.getBlockTileEntity(i, j, k));

		if (engine != null) {
			engine.delete();
		}

		super.onBlockRemoval(world, i, j, k);
	}
	 
	@Override
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		
		if (entityplayer.getCurrentEquippedItem() != null 
				&& entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			tile.switchOrientation();
			return true;
		} else {
			if (entityplayer.getCurrentEquippedItem() != null) {
				if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
					return false;
				}
			}
			
			if (tile.engine instanceof EngineStone) {
				EnergyProxy.displayGUISteamEngine(entityplayer, tile);
				return true;
			} else if (tile.engine instanceof EngineIron) {
				EnergyProxy.displayGUICombustionEngine(entityplayer, tile);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		tile.orientation = Orientations.YPos.ordinal();
		tile.switchOrientation();		
	}
    
	@Override
	protected int damageDropped(int i) {
		return i;
	}
	
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		
		if (!tile.isBurning()) {
			return;
		}
		
        float f = (float)i + 0.5F;
        float f1 = (float)j + 0.0F + (random.nextFloat() * 6F) / 16F;
        float f2 = (float)k + 0.5F;
        float f3 = 0.52F;
        float f4 = random.nextFloat() * 0.6F - 0.3F;
        
        world.spawnParticle("reddust", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("reddust", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("reddust", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("reddust", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this, 1, 0));
		itemList.add(new ItemStack(this, 1, 1));
		itemList.add(new ItemStack(this, 1, 2));
	}
	
    @Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l)
    {
    	TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
    	
    	if (tile != null) {
    		tile.checkRedstonePower();
    	}
    }
}
