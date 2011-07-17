package net.minecraft.src.buildcraft.devel;

import net.minecraft.src.Block;
import net.minecraft.src.BlockChest;
import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

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
			inv.setInventorySlotContents(ind++, new ItemStack (Block.oreGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.obsidian, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.dyePowder, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.dyePowder, 64, 4));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.dyePowder, 64, 11));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.paper, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.stone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.coal, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (Block.glass, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.planks, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (Item.redstoneRepeater, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.torchRedstoneActive, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.redstone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.ingotIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.ingotGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.diamond, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.wood, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.bucketEmpty, 1));
		} else {
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
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.fillerBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.markerBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.builderBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.templateBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.templateItem, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.goldGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.ironGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.stoneGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.woodenGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftCore.diamondGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftEnergy.engineBlock, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftEnergy.engineBlock, 64, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftEnergy.engineBlock, 64, 2));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftEnergy.bucketOil, 1));
		}
		
		super.blockActivated(world, i, j, k, entityplayer);
		
		return true;
		
	}
}
