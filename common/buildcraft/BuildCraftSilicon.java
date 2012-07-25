/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import buildcraft.mod_BuildCraftCore;
import buildcraft.mod_BuildCraftSilicon;
import buildcraft.api.bptblocks.BptBlockInventory;
import buildcraft.api.bptblocks.BptBlockRotateMeta;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.CoreProxy;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemRedstoneChipset;
import buildcraft.silicon.BlockAssemblyTable;
import buildcraft.silicon.BlockLaser;
import buildcraft.silicon.GuiHandler;
import buildcraft.silicon.network.ConnectionHandler;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

public class BuildCraftSilicon {

	public static int laserBlockModel;

	public static Item redstoneChipset;
	public static BlockLaser laserBlock;
	public static BlockAssemblyTable assemblyTableBlock;

	private static boolean initialized = false;

	public static void load() {
		// Register connection handler
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());

		// Register gui handler
		MinecraftForge.setGuiHandler(mod_BuildCraftSilicon.instance, new GuiHandler());
	}

	public static void initialize() {
		if (initialized)
			return;

		initialized = true;

		mod_BuildCraftCore.initialize();

		Property laserId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("laser.id", DefaultProps.LASER_ID);

		Property assemblyTableId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("assemblyTable.id",
				DefaultProps.ASSEMBLY_TABLE_ID);

		BuildCraftCore.mainConfiguration.save();

		laserBlock = new BlockLaser(Integer.parseInt(laserId.value));
		CoreProxy.addName(laserBlock.setBlockName("laserBlock"), "Laser");
		CoreProxy.registerBlock(laserBlock);

		assemblyTableBlock = new BlockAssemblyTable(Integer.parseInt(assemblyTableId.value));
		CoreProxy.addName(assemblyTableBlock.setBlockName("assemblyTableBlock"), "Assembly Table");
		CoreProxy.registerBlock(assemblyTableBlock);

		redstoneChipset = new ItemRedstoneChipset(DefaultProps.REDSTONE_CHIPSET);
		redstoneChipset.setItemName("redstoneChipset");

		// / REDSTONE CHIPSETS
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone) }, 10000,
				new ItemStack(redstoneChipset, 1, 0)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 0), "Redstone Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.ingotIron) }, 20000, new ItemStack(redstoneChipset, 1, 1)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 1), "Redstone Iron Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.ingotGold) }, 40000, new ItemStack(redstoneChipset, 1, 2)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 2), "Redstone Golden Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.diamond) }, 80000, new ItemStack(redstoneChipset, 1, 3)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 3), "Redstone Diamond Chipset");
		// PULSATING CHIPSETS
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.enderPearl) }, 40000, new ItemStack(redstoneChipset, 2, 4)));
		CoreProxy.addName(new ItemStack(redstoneChipset, 1, 4), "Pulsating Chipset");

		// / REDSTONE GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 0) }, 20000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 0)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 0), "Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 0), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 10000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 0)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 0), "Autarchic Gate");

		// / IRON AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 1),
				new ItemStack(BuildCraftTransport.redPipeWire) }, 40000, new ItemStack(BuildCraftTransport.pipeGate, 1, 1)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 1), "Iron AND Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 1), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 20000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1), "Autarchic Iron AND Gate");

		// / IRON OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 1),
				new ItemStack(BuildCraftTransport.redPipeWire) }, 40000, new ItemStack(BuildCraftTransport.pipeGate, 1, 2)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 2), "Iron OR Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 2), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 20000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2), "Autarchic Iron OR Gate");

		// / GOLD AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 2),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire) }, 80000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 3)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 3), "Gold AND Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 3), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 40000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3), "Autarchic Gold AND Gate");

		// / GOLD OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 2),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire) }, 80000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 4)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 4), "Gold OR Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 4), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 40000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4), "Autarchic Gold OR Gate");

		// / DIAMOND AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 3),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire),
				new ItemStack(BuildCraftTransport.greenPipeWire), new ItemStack(BuildCraftTransport.yellowPipeWire) }, 160000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 5)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 5), "Diamond AND Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 5), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 80000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5), "Autarchic Diamond AND Gate");

		// / DIAMOND OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 3),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire),
				new ItemStack(BuildCraftTransport.greenPipeWire), new ItemStack(BuildCraftTransport.yellowPipeWire) }, 160000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 6)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 6), "Diamond OR Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 6), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 80000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6)));
		CoreProxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6), "Autarchic Diamond OR Gate");

		new BptBlockRotateMeta(laserBlock.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockInventory(assemblyTableBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();

	}

	public static void loadRecipes() {
		CraftingManager craftingmanager = CraftingManager.getInstance();

		CoreProxy.addCraftingRecipe(new ItemStack(laserBlock), new Object[] { "ORR", "DDR", "ORR", Character.valueOf('O'),
				Block.obsidian, Character.valueOf('R'), Item.redstone, Character.valueOf('D'), Item.diamond, });

		CoreProxy.addCraftingRecipe(new ItemStack(assemblyTableBlock),
				new Object[] { "ORO", "ODO", "OGO", Character.valueOf('O'), Block.obsidian, Character.valueOf('R'),
						Item.redstone, Character.valueOf('D'), Item.diamond, Character.valueOf('G'),
						BuildCraftCore.diamondGearItem, });
		
		//Add reverse recipies for all gates
		
		//Iron
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 2), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 1)});
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 1), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 2)});
		
		//Gold
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 4), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 3)});
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 3), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 4)});

		//Diamond
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 6), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 5)});
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 5), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 6)});
		
		//Iron - Autarchic
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1)});
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2)});
		
		//Gold - Autarchic
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3)});
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4)});

		//Diamond - Autarchic
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5)});
		CoreProxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5), 
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6)});
		
		

	}

	public static void initializeModel(BaseMod mod) {
		laserBlockModel = ModLoader.getUniqueBlockModelID(mod, true);
	}
}
