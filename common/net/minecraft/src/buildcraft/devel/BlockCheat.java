/** 
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

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

	@Override
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		IInventory inv = (IInventory) world.getBlockTileEntity(i, j, k);

		
		entityplayer.experienceLevel = 50;
		
		int ind = 0;

		if (world.getBlockId(i + 1, j, k) == blockID
				|| world.getBlockId(i, j, k + 1) == blockID) {

			inv.setInventorySlotContents(ind++, new ItemStack(Item.pickaxeStone, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.axeDiamond, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.coal, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.planks, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.glass, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.redstone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.ingotIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.diamond, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.oreIron, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.bed, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsStone, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftBuilders.templateItem, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftBuilders.blueprintItem, 1));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.redPipeWire, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.bluePipeWire, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.yellowPipeWire, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeLiquidsStone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeLiquidsIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftFactory.tankBlock, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeGate, 64, 5));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeGate, 64, 6));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeGate, 64, 2));			
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeGate, 64, 3));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeGate, 64, 4));						
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftBuilders.markerBlock, 64));
		} else {			
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftBuilders.pathMarkerBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftFactory.autoWorkbenchBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Item.seeds, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.architectBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.builderBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.engineBlock, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.engineBlock, 64, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.engineBlock, 64, 2));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftCore.wrenchItem, 1));			
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsWood, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsCobblestone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsDiamond, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsObsidian, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipePowerWood, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.chest, 64));			
			inv.setInventorySlotContents(ind++, new ItemStack (Block.torchWood, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.stairSingle, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipePowerGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftBuilders.libraryBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (Block.enchantmentTable, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftEnergy.bucketOil, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftFactory.tankBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeLiquidsWood, 64));									
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeLiquidsCobblestone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeLiquidsGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack (BuildCraftTransport.pipeItemsStipes, 64));

		}

		super.blockActivated(world, i, j, k, entityplayer);

		return true;

	}
}
