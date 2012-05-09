/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.forge.ITextureProvider;

public class BlockBlueprintLibrary extends BlockContainer implements ITextureProvider {

	
	public BlockBlueprintLibrary(int i) {
		super(i, Material.wood);
		setHardness(0.7F);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

    @Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	
		// Drop through if the player is sneaking
		if(entityplayer.isSneaking())
			return false;
    	
    	TileBlueprintLibrary tile = (TileBlueprintLibrary) world.getBlockTileEntity(i, j, k);
    	
    	if (!tile.locked || entityplayer.username.equals(tile.owner))
    		if(!APIProxy.isClient(world))
    			entityplayer.openGui(mod_BuildCraftBuilders.instance, GuiIds.BLUEPRINT_LIBRARY, world, i, j, k);
		
		return true;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileBlueprintLibrary ();
	}
	
	@Override
	public int getBlockTextureFromSide(int i) {
		switch (i) {
		case 0:
		case 1:
			return 3 * 16 + 5;
		default:
			return 3 * 16 + 8;
		}
	}
	

    @Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
    	if (entityliving instanceof EntityPlayer) {
    		TileBlueprintLibrary tile = (TileBlueprintLibrary) world.getBlockTileEntity(i, j, k);
    		
    		tile.owner = ((EntityPlayer) entityliving).username;
    	}
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
