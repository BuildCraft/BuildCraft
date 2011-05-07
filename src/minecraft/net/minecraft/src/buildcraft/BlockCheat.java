package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class BlockCheat extends Block {
	
	public int texture;
	
	public BlockCheat(int i) {
		super(i, Material.iron);		
		    	
	}
	
	EntityMechanicalArm entity;
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
						
		createItems (i, j, k, new ItemStack (Item.redstone, 64));
		createItems (i, j, k, new ItemStack (Item.ingotIron, 64));
		createItems (i, j, k, new ItemStack (Item.ingotIron, 64));
		createItems (i, j, k, new ItemStack (Item.ingotGold, 64));
		createItems (i, j, k, new ItemStack (Item.diamond, 64));
		createItems (i, j, k, new ItemStack (Item.coal, 64));						
		
		createItems (i, j, k, new ItemStack (Block.glass, 64));
		createItems (i, j, k, new ItemStack (Block.planks, 64));
		createItems (i, j, k, new ItemStack (Block.oreGold, 64));
		createItems (i, j, k, new ItemStack (Block.oreIron, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().miningWellBlock, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().machineBlock, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().woodenPipeBlock, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().stonePipeBlock, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().ironPipeBlock, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().goldenPipeBlock, 64));
		createItems (i, j, k, new ItemStack (mod_BuildCraft.getInstance().diamondPipeBlock, 64));
		
		
		return false;
		
	}
	
	public void createItems (int i, int j, int k, ItemStack items) {				
		World w = ModLoader.getMinecraftInstance().theWorld;		
		
		float f = w.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = w.rand.nextFloat() * 0.8F + 0.1F;
		float f2 = w.rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityitem = new EntityItem(w, (float) i + f,
				(float) j + f1 + 0.5F, (float) k + f2, items);

		float f3 = 0.05F;
		entityitem.motionX = (float) w.rand.nextGaussian() * f3;
		entityitem.motionY = (float) w.rand.nextGaussian() * f3
				+ 0.3F;
		entityitem.motionZ = (float) w.rand.nextGaussian() * f3;
		w.entityJoinedWorld(entityitem);
		
	}
}
