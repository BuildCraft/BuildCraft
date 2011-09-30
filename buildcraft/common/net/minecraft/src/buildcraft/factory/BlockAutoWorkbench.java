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
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockAutoWorkbench extends BlockContainer implements
		ITextureProvider {

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

		FactoryProxy.displayGUIAutoCrafting(world, entityplayer, i, j, k);

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
