/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.mod_BuildCraftFactory;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.GuiIds;


public class BlockAutoWorkbench extends BlockContainer implements
		ITextureProvider {
	
	@Override
	public void addCreativeItems(ArrayList a) {
		a.add(new ItemStack(this, 1));
	}

	int topTexture;
	int sideTexture;
	
    public BlockAutoWorkbench(int i)
    {
        super(i, Material.wood);
        topTexture = 2 * 16 + 11;
        sideTexture = 2 * 16 + 12;
        setHardness(1.0F);
    }

    public int getBlockTextureFromSide(int i)
    {
        if(i == 1 || i == 0)
        {
			return topTexture;
        } else {
        	return sideTexture;
        }
    }

	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		super.blockActivated(world, i, j, k, entityplayer);

		if(!APIProxy.isClient(world))
			entityplayer.openGui(mod_BuildCraftFactory.instance, GuiIds.AUTO_CRAFTING_TABLE, world, i, j, k);

		return true;
	}

    
	@Override
	public TileEntity getBlockEntity() {
		return new TileAutoWorkbench ();
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
