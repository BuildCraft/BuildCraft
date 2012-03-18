/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.api.bptblocks.BptBlockInventory;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockRotateMeta;
import net.minecraft.src.buildcraft.core.AssemblyRecipe;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemRedstoneChipset;
import net.minecraft.src.buildcraft.silicon.BlockAssemblyTable;
import net.minecraft.src.buildcraft.silicon.BlockLaser;
import net.minecraft.src.forge.Property;

public class BuildCraftSilicon {

	public static int laserBlockModel;
	
	public static Item redstoneChipset;
	public static BlockLaser laserBlock;
	public static BlockAssemblyTable assemblyTableBlock;

	private static boolean initialized = false;

	public static void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;

		mod_BuildCraftCore.initialize();
		
		Property laserId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("laser.id", DefaultProps.LASER_ID);

		Property assemblyTableId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("assemblyTable.id",
						DefaultProps.ASSEMBLY_TABLE_ID);		
		
		laserBlock = new BlockLaser (Integer.parseInt(laserId.value));		
		CoreProxy.addName(laserBlock.setBlockName("laserBlock"),
		"Laser");
		CoreProxy.registerBlock(laserBlock);
		
		assemblyTableBlock = new BlockAssemblyTable (Integer.parseInt(assemblyTableId.value));
		CoreProxy.addName(assemblyTableBlock.setBlockName("assemblyTableBlock"),
		"Assembly Table");
		CoreProxy.registerBlock(assemblyTableBlock);

		redstoneChipset = new ItemRedstoneChipset(DefaultProps.REDSTONE_CHIPSET);
		redstoneChipset.setItemName("redstoneChipset");

		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(
				new ItemStack[] { new ItemStack(Item.redstone) }, 10000,
				new ItemStack(redstoneChipset, 1, 0)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 0),
				"Redstone Chipset");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.redstone), new ItemStack(Item.ingotIron) },
				20000, new ItemStack(redstoneChipset, 1, 1)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 1),
				"Redstone Iron Chipset");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.redstone), new ItemStack(Item.ingotGold) },
				40000, new ItemStack(redstoneChipset, 1, 2)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 2),
				"Redstone Golden Chipset");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.redstone), new ItemStack(Item.diamond) },
				80000, new ItemStack(redstoneChipset, 1, 3)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 3),
				"Redstone Diamond Chipset");

		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(
				new ItemStack[] { new ItemStack(redstoneChipset, 1, 0) },
				20000, new ItemStack(BuildCraftTransport.pipeGate, 1, 0)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 0),
				"Gate");

		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(redstoneChipset, 1, 1),
				new ItemStack(BuildCraftTransport.redPipeWire) }, 40000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 1)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 1),
				"Iron AND Gate");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(redstoneChipset, 1, 1),
				new ItemStack(BuildCraftTransport.redPipeWire) }, 40000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 2)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 2),
				"Iron OR Gate");

		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(redstoneChipset, 1, 2),
				new ItemStack(BuildCraftTransport.redPipeWire),
				new ItemStack(BuildCraftTransport.bluePipeWire) }, 80000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 3)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 3),
				"Gold AND Gate");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(redstoneChipset, 1, 2),
				new ItemStack(BuildCraftTransport.redPipeWire),
				new ItemStack(BuildCraftTransport.bluePipeWire) }, 80000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 4)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 4),
				"Gold OR Gate");

		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(redstoneChipset, 1, 3),
				new ItemStack(BuildCraftTransport.redPipeWire),
				new ItemStack(BuildCraftTransport.bluePipeWire),
				new ItemStack(BuildCraftTransport.greenPipeWire),
				new ItemStack(BuildCraftTransport.yellowPipeWire) }, 160000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 5)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 5),
				"Diamond AND Gate");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(redstoneChipset, 1, 3),
				new ItemStack(BuildCraftTransport.redPipeWire),
				new ItemStack(BuildCraftTransport.bluePipeWire),
				new ItemStack(BuildCraftTransport.greenPipeWire),
				new ItemStack(BuildCraftTransport.yellowPipeWire) }, 160000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 6)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 6),
				"Diamond OR Gate");

		new BptBlockRotateMeta(laserBlock.blockID, new int [] {2, 5, 3, 4}, true);
		new BptBlockInventory(assemblyTableBlock.blockID);
		
		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

	}
	
	public static void loadRecipes () {
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		craftingmanager.addRecipe(
				new ItemStack(laserBlock),
				new Object[] { "ORR", "DDR", "ORR",
					Character.valueOf('O'), Block.obsidian,
					Character.valueOf('R'), Item.redstone,
					Character.valueOf('D'), Item.diamond,
				});
		
		craftingmanager.addRecipe(
				new ItemStack(assemblyTableBlock),
				new Object[] { "ORO", "ODO", "OGO",
					Character.valueOf('O'), Block.obsidian,
					Character.valueOf('R'), Item.redstone,
					Character.valueOf('D'), Item.diamond,
					Character.valueOf('G'), BuildCraftCore.diamondGearItem,
				});
	}

	public static void initializeModel(BaseMod mod) {
		laserBlockModel = ModLoader.getUniqueBlockModelID(mod, true);
	}
}
