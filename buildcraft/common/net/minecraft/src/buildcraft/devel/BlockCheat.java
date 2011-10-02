/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.devel;

import net.minecraft.src.Block;
import net.minecraft.src.BlockChest;
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

	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		IInventory inv = (IInventory) world.getBlockTileEntity(i, j, k);

		int ind = 0;

		if (world.getBlockId(i + 1, j, k) == blockID
				|| world.getBlockId(i, j, k + 1) == blockID) {

			inv.setInventorySlotContents(ind++, new ItemStack(Item.pickaxeDiamond, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.axeDiamond, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.brick, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.glass, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.chest, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.workbench, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.oreGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.obsidian, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.dyePowder, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.dyePowder, 64, 2));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.dyePowder, 64, 4));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.dyePowder, 64, 11));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.stone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.coal, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.planks, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.torchRedstoneActive, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.redstone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.ingotIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.ingotGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.diamond, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.wood, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack(Block.pistonBase, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.bed, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.bucketLava, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(Item.bucketWater, 1));
		} else {
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftFactory.miningWellBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftFactory.quarryBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftCore.diamondGearItem, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftFactory.autoWorkbenchBlock, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftBuilders.fillerBlock, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftBuilders.markerBlock, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftBuilders.builderBlock, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftBuilders.templateBlock, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftBuilders.templateItem, 1));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftCore.goldGearItem, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftCore.ironGearItem, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftCore.stoneGearItem, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftCore.woodenGearItem, 64));
			// inv.setInventorySlotContents(ind++, new ItemStack
			// (BuildCraftCore.diamondGearItem, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.engineBlock, 64, 0));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.engineBlock, 64, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.engineBlock, 64, 2));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.bucketOil, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftEnergy.bucketFuel, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftCore.wrenchItem, 1));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftFactory.pumpBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftFactory.tankBlock, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftFactory.refineryBlock, 64));

			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsWood, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsCobblestone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsStone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsDiamond, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeItemsObsidian, 64));

			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeLiquidsWood, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeLiquidsCobblestone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeLiquidsStone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeLiquidsIron, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipeLiquidsGold, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipePowerWood, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipePowerStone, 64));
			inv.setInventorySlotContents(ind++, new ItemStack(BuildCraftTransport.pipePowerGold, 64));
		}

		super.blockActivated(world, i, j, k, entityplayer);

		return true;

	}
}
