package net.minecraft.src.buildcraft.devel;

import net.minecraft.src.Block;
import net.minecraft.src.BlockChest;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftBuilders;

public class BlockCheat extends BlockChest {
	
	public int texture;
	
	public BlockCheat(int i) {
		super(i);		
		    	
	}
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		IInventory inv = (IInventory) world.getBlockTileEntity(i, j, k);
		
		int ind = 0;
		
		if (world.getBlockId(i + 1, j, k) == blockID
				|| world.getBlockId(i, j, k + 1) == blockID) {

			inv.setInventorySlotContents(ind++, new ItemStack (Item.pickaxeDiamond, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.brick, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.glass, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.chest, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.workbench, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.wood, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (Block.oreGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.oreIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.obsidian, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.dyePowder, 64, 4));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.paper, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.lever, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.railPowered, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.minecartCrate, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.lavaStill, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.stone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.bucketMilk, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.wheat, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.sugar, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.egg, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.coal, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (Block.glass, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.planks, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (Item.redstoneRepeater, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.torchRedstoneActive, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.redstone, 64));
		} else {
			inv.setInventorySlotContents(ind++, new ItemStack (Item.ingotIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.ingotGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.diamond, 64));		
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftFactory.miningWellBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftFactory.quarryBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftFactory.autoWorkbenchBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.woodenPipeBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.stonePipeBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.cobblestonePipeBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.ironPipeBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.goldenPipeBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.obsidianPipeBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.diamondPipeBlock, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (mod_BuildCraftBuilders.fillerBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (mod_BuildCraftBuilders.markerBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (mod_BuildCraftBuilders.builderBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (mod_BuildCraftBuilders.templateBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (mod_BuildCraftBuilders.templateItem, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.goldGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.ironGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.stoneGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.woodenGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.diamondGearItem, 64));
		}
		
		super.blockActivated(world, i, j, k, entityplayer);
		
		return true;
		
	}
}
