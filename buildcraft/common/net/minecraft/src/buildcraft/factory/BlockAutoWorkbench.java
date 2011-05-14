package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftFactory;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockAutoWorkbench extends BlockContainer
{
	
	BuildCraftBlockUtil p;

    public BlockAutoWorkbench(int i)
    {
        super(i, Material.wood);
        blockIndexInTexture = 59;
        setHardness(1.0F);
    }

    public int getBlockTextureFromSide(int i)
    {
        if(i == 1)
        {
			return mod_BuildCraftFactory.machineBlock.textureSide;
        }
        if(i == 0)
        {
            return Block.planks.getBlockTextureFromSide(0);
        }
        if(i == 2 || i == 4)
        {
            return blockIndexInTexture + 1;
        } else
        {
            return blockIndexInTexture;
        }
    }

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer)
    {
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
}
