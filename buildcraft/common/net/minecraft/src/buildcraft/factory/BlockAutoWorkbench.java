package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockAutoWorkbench extends BlockContainer
{
	
	BuildCraftBlockUtil p;

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
	protected TileEntity getBlockEntity() {
		return new TileAutoWorkbench ();
	}
	
    public void onBlockRemoval(World world, int i, int j, int k) {   
		Utils.dropItems(world,
				(IInventory) world.getBlockTileEntity(i, j, k), i, j, k);
    	
        super.onBlockRemoval(world, i, j, k);        
    }
    
    public int getRenderType() {
        return BuildCraftCore.customTextureModel;
    }
}
