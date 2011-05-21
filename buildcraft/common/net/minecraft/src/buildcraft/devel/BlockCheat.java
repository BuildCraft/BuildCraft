package net.minecraft.src.buildcraft.devel;

import net.minecraft.src.Block;
import net.minecraft.src.BlockChest;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftFactory;
import net.minecraft.src.buildcraft.core.CoreProxy;

public class BlockCheat extends BlockChest {
	
	public int texture;
	
	public BlockCheat(int i) {
		super(i);		
		    	
	}
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		IInventory inv = (IInventory) world.getBlockTileEntity(i, j, k);
		
		inv.setInventorySlotContents(0, new ItemStack (Item.redstone, 64));
		inv.setInventorySlotContents(1, new ItemStack (Item.ingotIron, 64));
		inv.setInventorySlotContents(2, new ItemStack (Item.ingotIron, 64));
		inv.setInventorySlotContents(3, new ItemStack (Item.ingotGold, 64));
		inv.setInventorySlotContents(4, new ItemStack (Item.diamond, 64));
		inv.setInventorySlotContents(5, new ItemStack (Item.coal, 64));						
		
		inv.setInventorySlotContents(6, new ItemStack (Block.glass, 64));
		inv.setInventorySlotContents(7, new ItemStack (Block.planks, 64));
		inv.setInventorySlotContents(8, new ItemStack (Block.oreGold, 64));
		inv.setInventorySlotContents(9, new ItemStack (Block.oreIron, 64));
		inv.setInventorySlotContents(10, new ItemStack (Block.cloth, 64, 1));
		inv.setInventorySlotContents(11, new ItemStack (Block.cloth, 64, 2));
		inv.setInventorySlotContents(12, new ItemStack (Item.redstoneRepeater, 64));
		inv.setInventorySlotContents(13, new ItemStack (Block.torchRedstoneActive, 64));
		inv.setInventorySlotContents(14, new ItemStack (Block.dispenser, 64, 3));
		inv.setInventorySlotContents(15, new ItemStack (mod_BuildCraftFactory.miningWellBlock, 64));
		inv.setInventorySlotContents(16, new ItemStack (mod_BuildCraftFactory.machineBlock, 64));
		inv.setInventorySlotContents(17, new ItemStack (BuildCraftTransport.woodenPipeBlock, 64));
		inv.setInventorySlotContents(18, new ItemStack (BuildCraftTransport.stonePipeBlock, 64));
		inv.setInventorySlotContents(19, new ItemStack (BuildCraftTransport.ironPipeBlock, 64));
		inv.setInventorySlotContents(20, new ItemStack (BuildCraftTransport.goldenPipeBlock, 64));
		inv.setInventorySlotContents(21, new ItemStack (BuildCraftTransport.diamondPipeBlock, 64));
		inv.setInventorySlotContents(22, new ItemStack (Block.mobSpawner, 64));
		
		super.blockActivated(world, i, j, k, entityplayer);
		
		return true;
		
	}
}
