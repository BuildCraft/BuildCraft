package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiCrafting;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 


public class BlockAutoWorkbench extends BlockContainer
{

    public BlockAutoWorkbench(int i)
    {
        super(i, Material.iron);
        blockIndexInTexture = 59;
    }

    public int getBlockTextureFromSide(int i)
    {
        if(i == 1)
        {
			return mod_BuildCraft.getInstance().machineBlock.textureSide;
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
        if(world.multiplayerWorld)
        {
            return true;
        } else {
        	ModLoader.getMinecraftInstance().displayGuiScreen(
					new GuiAutoCrafting(entityplayer.inventory, world, i, j, k));
            return true;
        }
    }

	@Override
	protected TileEntity getBlockEntity() {
		return new TileAutoWorkbench ();
	}
}
