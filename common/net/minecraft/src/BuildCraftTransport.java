/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.core.AssemblyRecipe;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemBuildCraft;
import net.minecraft.src.buildcraft.transport.ActionEnergyPulser;
import net.minecraft.src.buildcraft.transport.ActionSignalOutput;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.BptBlockPipe;
import net.minecraft.src.buildcraft.transport.BptItemPipeDiamond;
import net.minecraft.src.buildcraft.transport.BptItemPipeIron;
import net.minecraft.src.buildcraft.transport.BptItemPipeWodden;
import net.minecraft.src.buildcraft.transport.GuiHandler;
import net.minecraft.src.buildcraft.transport.ItemGate;
import net.minecraft.src.buildcraft.transport.LegacyBlock;
import net.minecraft.src.buildcraft.transport.LegacyTile;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicWood;
import net.minecraft.src.buildcraft.transport.PipeTriggerProvider;
import net.minecraft.src.buildcraft.transport.TileDummyGenericPipe;
import net.minecraft.src.buildcraft.transport.TileDummyGenericPipe2;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.buildcraft.transport.TriggerPipeContents;
import net.minecraft.src.buildcraft.transport.TriggerPipeContents.Kind;
import net.minecraft.src.buildcraft.transport.TriggerPipeSignal;
import net.minecraft.src.buildcraft.transport.network.ConnectionHandler;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsCobblestone;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsDiamond;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsGold;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsIron;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsObsidian;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsStone;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsStripes;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsWood;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsCobblestone;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsGold;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsIron;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsStone;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsWood;
import net.minecraft.src.buildcraft.transport.pipes.PipePowerGold;
import net.minecraft.src.buildcraft.transport.pipes.PipePowerStone;
import net.minecraft.src.buildcraft.transport.pipes.PipePowerWood;
import net.minecraft.src.buildcraft.transport.pipes.PipeStructureCobblestone;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

public class BuildCraftTransport {

	private static boolean initialized = false;

	public static BlockGenericPipe genericPipeBlock;

	public static int [] diamondTextures = new int [6];

	public static boolean alwaysConnectPipes;
	public static int maxItemsInPipes;

	public static Item pipeWaterproof;
	public static Item pipeGate;
	public static Item pipeGateAutarchic;
	public static Item redPipeWire;
	public static Item bluePipeWire;
	public static Item greenPipeWire;
	public static Item yellowPipeWire;

	public static Item pipeItemsWood;
	public static Item pipeItemsStone;
	public static Item pipeItemsCobblestone;
	public static Item pipeItemsIron;
	public static Item pipeItemsGold;
	public static Item pipeItemsDiamond;
	public static Item pipeItemsObsidian;

	public static Item pipeLiquidsWood;
	public static Item pipeLiquidsCobblestone;
	public static Item pipeLiquidsStone;
	public static Item pipeLiquidsIron;
	public static Item pipeLiquidsGold;

	public static Item pipePowerWood;
	public static Item pipePowerStone;
	public static Item pipePowerGold;

	public static Item pipeItemsStipes;
	public static Item pipeStructureCobblestone;
	public static int groupItemsTrigger;

	public static Trigger triggerPipeEmpty = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_EMPTY, Kind.Empty);
	public static Trigger triggerPipeItems = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_ITEMS, Kind.ContainsItems);
	public static Trigger triggerPipeLiquids = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_LIQUIDS, Kind.ContainsLiquids);
	public static Trigger triggerPipeEnergy = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_ENERGY, Kind.ContainsEnergy);
	public static Trigger triggerRedSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_RED_SIGNAL_ACTIVE, true, IPipe.WireColor.Red);
	public static Trigger triggerRedSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_RED_SIGNAL_INACTIVE, false, IPipe.WireColor.Red);
	public static Trigger triggerBlueSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_BLUE_SIGNAL_ACTIVE, true, IPipe.WireColor.Blue);
	public static Trigger triggerBlueSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_BLUE_SIGNAL_INACTIVE, false, IPipe.WireColor.Blue);
	public static Trigger triggerGreenSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_GREEN_SIGNAL_ACTIVE, true, IPipe.WireColor.Green);
	public static Trigger triggerGreenSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_GREEN_SIGNAL_INACTIVE, false, IPipe.WireColor.Green);
	public static Trigger triggerYellowSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_YELLOW_SIGNAL_ACTIVE, true, IPipe.WireColor.Yellow);
	public static Trigger triggerYellowSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_YELLOW_SIGNAL_INACTIVE, false, IPipe.WireColor.Yellow);

	public static Action actionRedSignal = new ActionSignalOutput(DefaultProps.ACTION_RED_SIGNAL, IPipe.WireColor.Red);
	public static Action actionBlueSignal = new ActionSignalOutput(DefaultProps.ACTION_BLUE_SIGNAL, IPipe.WireColor.Blue);
	public static Action actionGreenSignal = new ActionSignalOutput(DefaultProps.ACTION_GREEN_SIGNAL, IPipe.WireColor.Green);
	public static Action actionYellowSignal = new ActionSignalOutput(DefaultProps.ACTION_YELLOW_SIGNAL, IPipe.WireColor.Yellow);
	public static Action actionEnergyPulser = new ActionEnergyPulser(DefaultProps.ACTION_ENERGY_PULSER);

	private static class PipeRecipe {
		boolean isShapeless = false; // pipe recipes come shaped and unshaped.
		ItemStack result;
		Object [] input;
	}

	private static LinkedList <PipeRecipe> pipeRecipes = new LinkedList <PipeRecipe> ();

	public static void load() {
		// Register connection handler
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());

		// Register gui handler
		MinecraftForge.setGuiHandler(mod_BuildCraftTransport.instance, new GuiHandler());
	}

	public static void initialize () {
		if (initialized)
			return;

		initialized = true;

		mod_BuildCraftCore.initialize();

		Property loadLegacyPipes = BuildCraftCore.mainConfiguration
		.getOrCreateBooleanProperty("loadLegacyPipes", Configuration.CATEGORY_GENERAL, true);
		loadLegacyPipes.comment = "set to true to load pre 2.2.5 worlds pipes";

		Property alwaysConnect = BuildCraftCore.mainConfiguration
				.getOrCreateBooleanProperty("pipes.alwaysConnect",
						Configuration.CATEGORY_GENERAL,
						DefaultProps.PIPES_ALWAYS_CONNECT);
		alwaysConnect.comment = "set to false to deactivate pipe connection rules, true by default";

		Property exclusionList = BuildCraftCore.mainConfiguration
				.getOrCreateProperty("woodenPipe.exclusion",
						Configuration.CATEGORY_BLOCK, "");

		PipeLogicWood.excludedBlocks = exclusionList.value.split(",");

		Property maxItemInPipesProp = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("pipes.maxItems",
				Configuration.CATEGORY_GENERAL,
				100);
		maxItemInPipesProp.comment = "pipes containing more than this amount of items will explode, not dropping any item";

		maxItemsInPipes = Integer.parseInt(maxItemInPipesProp.value);

		Property groupItemsTriggerProp = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("pipes.groupItemsTrigger",
				Configuration.CATEGORY_GENERAL,
				32);
		groupItemsTriggerProp.comment = "when reaching this amount of objects in a pipes, items will be automatically grouped";

		groupItemsTrigger = Integer.parseInt(groupItemsTriggerProp.value);


		Property genericPipeId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("pipe.id",
				DefaultProps.GENERIC_PIPE_ID);

		for (int j = 0; j < PipeLogicWood.excludedBlocks.length; ++j)
			PipeLogicWood.excludedBlocks[j] = PipeLogicWood.excludedBlocks[j]
					.trim();

		BuildCraftCore.mainConfiguration.save();

		pipeWaterproof = new ItemBuildCraft (DefaultProps.PIPE_WATERPROOF_ID).setIconIndex(2 * 16 + 1);
		pipeWaterproof.setItemName("pipeWaterproof");
		CoreProxy.addName(pipeWaterproof, "Pipe Waterproof");
		genericPipeBlock = new BlockGenericPipe(Integer.parseInt(genericPipeId.value));
		CoreProxy.registerBlock(genericPipeBlock);

		// Fixing retro-compatiblity
		mod_BuildCraftTransport.registerTilePipe(TileDummyGenericPipe.class,
				"net.minecraft.src.buildcraft.GenericPipe");
		mod_BuildCraftTransport.registerTilePipe(TileDummyGenericPipe2.class,
				"net.minecraft.src.buildcraft.transport.TileGenericPipe");

		mod_BuildCraftTransport.registerTilePipe(TileGenericPipe.class,
				"net.minecraft.src.buildcraft.transport.GenericPipe");

		pipeItemsWood = createPipe (DefaultProps.PIPE_ITEMS_WOOD_ID, PipeItemsWood.class, "Wooden Transport Pipe", Block.planks, Block.glass, Block.planks);
		pipeItemsCobblestone = createPipe(DefaultProps.PIPE_ITEMS_COBBLESTONE_ID, PipeItemsCobblestone.class, "Cobblestone Transport Pipe", Block.cobblestone, Block.glass, Block.cobblestone);
		pipeItemsStone = createPipe (DefaultProps.PIPE_ITEMS_STONE_ID, PipeItemsStone.class, "Stone Transport Pipe", Block.stone, Block.glass, Block.stone);
		pipeItemsIron = createPipe (DefaultProps.PIPE_ITEMS_IRON_ID, PipeItemsIron.class, "Iron Transport Pipe", Item.ingotIron, Block.glass, Item.ingotIron);
		pipeItemsGold = createPipe (DefaultProps.PIPE_ITEMS_GOLD_ID, PipeItemsGold.class, "Golden Transport Pipe", Item.ingotGold, Block.glass, Item.ingotGold);
		pipeItemsDiamond = createPipe (DefaultProps.PIPE_ITEMS_DIAMOND_ID, PipeItemsDiamond.class, "Diamond Transport Pipe", Item.diamond, Block.glass, Item.diamond);
		pipeItemsObsidian = createPipe (DefaultProps.PIPE_ITEMS_OBSIDIAN_ID, PipeItemsObsidian.class, "Obsidian Transport Pipe", Block.obsidian, Block.glass, Block.obsidian);

		pipeLiquidsWood = createPipe (DefaultProps.PIPE_LIQUIDS_WOOD_ID, PipeLiquidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood, null);
		pipeLiquidsCobblestone = createPipe (DefaultProps.PIPE_LIQUIDS_COBBLESTONE_ID, PipeLiquidsCobblestone.class, "Cobblestone Waterproof Pipe", pipeWaterproof, pipeItemsCobblestone, null);
		pipeLiquidsStone = createPipe (DefaultProps.PIPE_LIQUIDS_STONE_ID, PipeLiquidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone, null);
		pipeLiquidsIron = createPipe (DefaultProps.PIPE_LIQUIDS_IRON_ID, PipeLiquidsIron.class, "Iron Waterproof Pipe", pipeWaterproof, pipeItemsIron, null);
		pipeLiquidsGold = createPipe (DefaultProps.PIPE_LIQUIDS_GOLD_ID, PipeLiquidsGold.class, "Golden Waterproof Pipe", pipeWaterproof, pipeItemsGold, null);
		// diamond
		// obsidian

		pipePowerWood = createPipe (DefaultProps.PIPE_POWER_WOOD_ID, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone,  pipeItemsWood, null);
		// cobblestone
		pipePowerStone = createPipe (DefaultProps.PIPE_POWER_STONE_ID, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone, null);
		// iron
		pipePowerGold = createPipe(DefaultProps.PIPE_POWER_GOLD_ID, PipePowerGold.class, "Golden Conductive Pipe", Item.redstone, pipeItemsGold, null);
		// diamond
		// obsidian

		// Fix name and recipe (Structure pipe insteand of Signal?)
		pipeStructureCobblestone = createPipe (DefaultProps.PIPE_STRUCTURE_COBBLESTONE_ID, PipeStructureCobblestone.class, "Cobblestone Structure Pipe", Block.gravel,  pipeItemsCobblestone, null);

		// Fix the recipe
		pipeItemsStipes = createPipe (DefaultProps.PIPE_ITEMS_STRIPES_ID, PipeItemsStripes.class, "Stripes Transport Pipe", new ItemStack (Item.dyePowder, 1, 0),  Block.glass, new ItemStack (Item.dyePowder, 1, 11));

//		dockingStationBlock = new BlockDockingStation(Integer.parseInt(dockingStationId.value));
//		ModLoader.registerBlock(dockingStationBlock);
//		CoreProxy.addName(dockingStationBlock.setBlockName("dockingStation"),
//		"Docking Station");

//		ModLoader.RegisterTileEntity(TileDockingStation.class, "net.minecraft.src.buildcraft.TileDockingStation");

		for (int j = 0; j < 6; ++j)
			diamondTextures [j] = 1 * 16 + 6 + j;

		redPipeWire = new ItemBuildCraft(DefaultProps.RED_PIPE_WIRE).setIconIndex(4 * 16 + 0);
		redPipeWire.setItemName("redPipeWire");
		CoreProxy.addName(redPipeWire, "Red Pipe Wire");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.dyePowder, 1, 1),
				new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(
				redPipeWire, 8)));

		bluePipeWire = new ItemBuildCraft(DefaultProps.BLUE_PIPE_WIRE).setIconIndex(4 * 16 + 1);
		bluePipeWire.setItemName("bluePipeWire");
		CoreProxy.addName(bluePipeWire, "Blue Pipe Wire");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.dyePowder, 1, 4),
				new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(
				bluePipeWire, 8)));

		greenPipeWire = new ItemBuildCraft(DefaultProps.GREEN_PIPE_WIRE).setIconIndex(4 * 16 + 2);
		greenPipeWire.setItemName("greenPipeWire");
		CoreProxy.addName(greenPipeWire, "Green Pipe Wire");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.dyePowder, 1, 2),
				new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(
				greenPipeWire, 8)));

		yellowPipeWire = new ItemBuildCraft(DefaultProps.YELLOW_PIPE_WIRE).setIconIndex(4 * 16 + 3);
		yellowPipeWire.setItemName("yellowPipeWire");
		CoreProxy.addName(yellowPipeWire, "Yellow Pipe Wire");
		BuildCraftCore.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(Item.dyePowder, 1, 11),
				new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(
				yellowPipeWire, 8)));

		pipeGate = new ItemGate(DefaultProps.GATE_ID, 0).setIconIndex(2 * 16 + 3);
		pipeGate.setItemName("pipeGate");

		pipeGateAutarchic = new ItemGate(DefaultProps.GATE_AUTARCHIC_ID, 1).setIconIndex(2 * 16 + 3);
		pipeGateAutarchic.setItemName("pipeGateAutarchic");

		alwaysConnectPipes = Boolean.parseBoolean(alwaysConnect.value);

		if (loadLegacyPipes.value.equals("true")) {
			Property woodenPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("woodenPipe.id",
							DefaultProps.WOODEN_PIPE_ID);
			Property stonePipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("stonePipe.id",
							DefaultProps.STONE_PIPE_ID);
			Property ironPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("ironPipe.id",
							DefaultProps.IRON_PIPE_ID);
			Property goldenPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("goldenPipe.id",
							DefaultProps.GOLDEN_PIPE_ID);
			Property diamondPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("diamondPipe.id",
							DefaultProps.DIAMOND_PIPE_ID);
			Property obsidianPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("obsidianPipe.id",
							DefaultProps.OBSIDIAN_PIPE_ID);
			Property cobblestonePipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("cobblestonePipe.id",
							DefaultProps.COBBLESTONE_PIPE_ID);

			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(woodenPipeId.value), pipeItemsWood.shiftedIndex));
			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(stonePipeId.value), pipeItemsStone.shiftedIndex));
			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(ironPipeId.value), pipeItemsIron.shiftedIndex));
			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(goldenPipeId.value), pipeItemsGold.shiftedIndex));
			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(diamondPipeId.value), pipeItemsDiamond.shiftedIndex));
			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(obsidianPipeId.value), pipeItemsObsidian.shiftedIndex));
			CoreProxy.registerBlock(new LegacyBlock(Integer
					.parseInt(cobblestonePipeId.value), pipeItemsCobblestone.shiftedIndex));

			CoreProxy.registerTileEntity(LegacyTile.class,
							"net.buildcraft.src.buildcraft.transport.legacy.LegacyTile");
		}

		BuildCraftCore.mainConfiguration.save();

		new BptBlockPipe (genericPipeBlock.blockID);

		BuildCraftCore.itemBptProps [pipeItemsWood.shiftedIndex] = new BptItemPipeWodden();
		BuildCraftCore.itemBptProps [pipeLiquidsWood.shiftedIndex] = new BptItemPipeWodden();
		BuildCraftCore.itemBptProps [pipeItemsIron.shiftedIndex] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps [pipeLiquidsIron.shiftedIndex] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps [pipeItemsDiamond.shiftedIndex] = new BptItemPipeDiamond();

		BuildCraftAPI.registerTriggerProvider(new PipeTriggerProvider());


		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();
	}

	public static void loadRecipes () {
		CraftingManager craftingmanager = CraftingManager.getInstance();

		// Add base recipe for pipe waterproof.
		craftingmanager.addRecipe(new ItemStack(pipeWaterproof, 1), new Object[] {
			"W ", "  ",
			Character.valueOf('W'), new ItemStack(Item.dyePowder, 1, 2)});

		// Add pipe recipes
		for (PipeRecipe p : pipeRecipes)
			if(p.isShapeless)
				craftingmanager.addShapelessRecipe(p.result, p.input);
			else
				craftingmanager.addRecipe(p.result, p.input);
	}

	private static Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr, Object ingredient1, Object ingredient2, Object ingredient3) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0))
				+ clas.getSimpleName().substring(1);

		Property prop = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty(name + ".id",
						Configuration.CATEGORY_ITEM, defaultID);

		int id = Integer.parseInt(prop.value);
		Item res =  BlockGenericPipe.registerPipe (id, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);

		// Add appropriate recipe to temporary list
		PipeRecipe recipe = new PipeRecipe ();

		if (ingredient1 != null && ingredient2 != null && ingredient3 != null) {
			recipe.result = new ItemStack(res, 8);
			recipe.input = new Object[] {
				"   ", "ABC", "   ",
				Character.valueOf('A'), ingredient1,
				Character.valueOf('B'), ingredient2,
				Character.valueOf('C'), ingredient3};

			pipeRecipes.add(recipe);
		} else if (ingredient1 != null && ingredient2 != null) {
			recipe.isShapeless = true;
			recipe.result = new ItemStack(res, 1);
			recipe.input = new Object[] { ingredient1, ingredient2};

			pipeRecipes.add(recipe);
		}

		return res;
	}
}
