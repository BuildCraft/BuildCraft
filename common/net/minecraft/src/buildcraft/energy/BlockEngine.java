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
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftEnergy;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.GuiIds;

public class BlockEngine extends BlockContainer implements IPipeConnection {
	
	@Override
	public void addCreativeItems(ArrayList a) {
		a.add(new ItemStack(this, 1, 0));
		a.add(new ItemStack(this, 1, 1));
		a.add(new ItemStack(this, 1, 2));
	}
	
	public BlockEngine(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
	}
	
	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public boolean isACube () {
		return false;
	}

    public int getRenderType()
    {
    	return BuildCraftCore.blockByEntityModel;
    }

	@Override
	public TileEntity getBlockEntity() {
		return new TileEngine();
	}
	
	 public void onBlockRemoval(World world, int i, int j, int k) {
		 TileEngine engine = ((TileEngine) world.getBlockTileEntity(i, j, k));
		 
		 if (engine != null) {
			 engine.delete ();
		 }
	
		 super.onBlockRemoval(world, i, j, k);
	 }
	 
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		
		if (entityplayer.getCurrentEquippedItem() != null 
				&& entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			tile.switchOrientation();
			return true;
		} else {
			if (tile.engine instanceof EngineStone) {
				if(!APIProxy.isClient(tile.worldObj))
					entityplayer.openGui(mod_BuildCraftEnergy.instance, GuiIds.ENGINE_STONE, world, i, j, k);
				return true;
			} else if (tile.engine instanceof EngineIron) {
				if(!APIProxy.isClient(tile.worldObj))
					entityplayer.openGui(mod_BuildCraftEnergy.instance, GuiIds.ENGINE_IRON, world, i, j, k);
				return true;
			}
		}
		
		return true;
	}
	
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		tile.orientation = Orientations.YPos.ordinal();
		tile.switchOrientation();		
	}
    
	protected int damageDropped(int i) {
		return i;
	}
	
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

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		TileEngine tile = (TileEngine) blockAccess.getBlockTileEntity(x1, y1, z1);
		
		if (tile == null) {
			return false;
		}
		
		if (tile.engine instanceof EngineWood) {
			return false;
		}
		
		switch (Orientations.values()[tile.orientation]) {
		case YPos:
			return y1 - y2 != -1;
		case YNeg:
			return y1 - y2 != 1;
		case ZPos:
			return z1 - z2 != -1;
		case ZNeg:
			return z1 - z2 != 1;
		case XPos:
			return x1 - x2 != -1;
		case XNeg:
			return x1 - x2 != 1;
		}
		
		return true;		
	}
}
