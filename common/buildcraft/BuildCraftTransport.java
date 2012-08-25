/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import java.util.LinkedList;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IExtractionHandler;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemBuildCraft;
import buildcraft.transport.ActionEnergyPulser;
import buildcraft.transport.ActionSignalOutput;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.BptBlockPipe;
import buildcraft.transport.BptItemPipeDiamond;
import buildcraft.transport.BptItemPipeIron;
import buildcraft.transport.BptItemPipeWodden;
import buildcraft.transport.GuiHandler;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemGate;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTriggerProvider;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TriggerPipeContents;
import buildcraft.transport.TriggerPipeSignal;
import buildcraft.transport.TriggerPipeContents.Kind;
import buildcraft.transport.network.PacketHandlerTransport;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsGold;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsSandstone;
import buildcraft.transport.pipes.PipeItemsStone;
//import buildcraft.transport.pipes.PipeItemsStripes;
import buildcraft.transport.pipes.PipeItemsVoid;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipeLiquidsCobblestone;
import buildcraft.transport.pipes.PipeLiquidsGold;
import buildcraft.transport.pipes.PipeLiquidsIron;
import buildcraft.transport.pipes.PipeLiquidsSandstone;
import buildcraft.transport.pipes.PipeLiquidsStone;
import buildcraft.transport.pipes.PipeLiquidsVoid;
import buildcraft.transport.pipes.PipeLiquidsWood;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.pipes.PipeStructureCobblestone;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

@Mod(version = DefaultProps.VERSION, modid="BuildCraft|Transport", name = "Buildcraft Transport", dependencies=DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels={DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerTransport.class)
public class BuildCraftTransport {
	public static BlockGenericPipe genericPipeBlock;

	public static int[] diamondTextures = new int[6];

	public static boolean alwaysConnectPipes;
	public static boolean usePipeLoss;
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
	public static Item pipeItemsVoid;
	public static Item pipeItemsSandstone;

	public static Item pipeLiquidsWood;
	public static Item pipeLiquidsCobblestone;
	public static Item pipeLiquidsStone;
	public static Item pipeLiquidsIron;
	public static Item pipeLiquidsGold;
	public static Item pipeLiquidsVoid;
	public static Item pipeLiquidsSandstone;

	public static Item pipePowerWood;
	public static Item pipePowerStone;
	public static Item pipePowerGold;

	public static Item facadeItem;

	//public static Item pipeItemsStipes;
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

	@Instance
	public static BuildCraftTransport instance;

	private static class PipeRecipe {
		boolean isShapeless = false; // pipe recipes come shaped and unshaped.
		ItemStack result;
		Object[] input;
	}

	private static class ExtractionHandler implements IExtractionHandler {
	      private final String[] items;
	      private final String[] liquids;

	      public ExtractionHandler(String[] items, String[] liquids){
	         this.items = items;
	         this.liquids = liquids;
	      }

	      @Override
	      public boolean canExtractItems(IPipe pipe, World world, int i, int j, int k) {
	         return testStrings(items, world, i, j, k);
	      }

	      @Override
	      public boolean canExtractLiquids(IPipe pipe, World world, int i, int j, int k) {
	         return testStrings(liquids, world, i, j, k);
	      }

	      private boolean testStrings(String[] excludedBlocks, World world, int i, int j, int k) {
	         int id = world.getBlockId(i, j, k);
	         Block block = Block.blocksList[id];
	         if(block == null)
	            return false;

	         int meta = world.getBlockMetadata(i, j, k);

	         for (String excluded : excludedBlocks) {
	            if (excluded.equals(block.getBlockName()))
	               return false;

	            String[] tokens = excluded.split(":");
	            if(tokens[0].equals(Integer.toString(id)) && (tokens.length == 1 || tokens[1].equals(Integer.toString(meta))))
	               return false;
	         }
	         return true;
	      }
	   }

	private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();

	@PreInit
	public void preInitialize(FMLPreInitializationEvent evt)
	{
		try
		{
			Property alwaysConnect = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("pipes.alwaysConnect", Configuration.CATEGORY_GENERAL, DefaultProps.PIPES_ALWAYS_CONNECT);
			alwaysConnect.comment = "set to false to deactivate pipe connection rules, true by default";
			alwaysConnectPipes = alwaysConnect.getBoolean(DefaultProps.PIPES_ALWAYS_CONNECT);

			Property pipeLoss = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("power.usePipeLoss", Configuration.CATEGORY_GENERAL, DefaultProps.USE_PIPELOSS);
			pipeLoss.comment = "Set to false to turn off energy loss over distance on all power pipes";
			usePipeLoss = pipeLoss.getBoolean(DefaultProps.USE_PIPELOSS);

			Property exclusionItemList = BuildCraftCore.mainConfiguration.getOrCreateProperty("woodenPipe.item.exclusion", Configuration.CATEGORY_BLOCK, "");

			String[] excludedItemBlocks = exclusionItemList.value.split(",");
			for (int j = 0; j < excludedItemBlocks.length; ++j)
			{
				excludedItemBlocks[j] = excludedItemBlocks[j].trim();
			}

			Property exclusionLiquidList = BuildCraftCore.mainConfiguration.getOrCreateProperty("woodenPipe.liquid.exclusion", Configuration.CATEGORY_BLOCK, "");

			String[] excludedLiquidBlocks = exclusionLiquidList.value.split(",");
			for (int j = 0; j < excludedLiquidBlocks.length; ++j)
			{
				excludedLiquidBlocks[j] = excludedLiquidBlocks[j].trim();
			}

			PipeManager.registerExtractionHandler(new ExtractionHandler(excludedItemBlocks, excludedLiquidBlocks));

			Property maxItemInPipesProp = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("pipes.maxItems", Configuration.CATEGORY_GENERAL, 100);
			maxItemInPipesProp.comment = "pipes containing more than this amount of items will explode, not dropping any item";

			maxItemsInPipes = Integer.parseInt(maxItemInPipesProp.value);

			Property groupItemsTriggerProp = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("pipes.groupItemsTrigger", Configuration.CATEGORY_GENERAL, 32);
			groupItemsTriggerProp.comment = "when reaching this amount of objects in a pipes, items will be automatically grouped";

			groupItemsTrigger = Integer.parseInt(groupItemsTriggerProp.value);

			Property genericPipeId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("pipe.id", DefaultProps.GENERIC_PIPE_ID);

			pipeWaterproof = new ItemBuildCraft(DefaultProps.PIPE_WATERPROOF_ID).setIconIndex(2 * 16 + 1);
			pipeWaterproof.setItemName("pipeWaterproof");
			LanguageRegistry.addName(pipeWaterproof, "Pipe Waterproof");
			genericPipeBlock = new BlockGenericPipe(Integer.parseInt(genericPipeId.value));
			GameRegistry.registerBlock(genericPipeBlock);

			// Fixing retro-compatiblity
			pipeItemsWood = createPipe(DefaultProps.PIPE_ITEMS_WOOD_ID, PipeItemsWood.class, "Wooden Transport Pipe", Block.planks, Block.glass, Block.planks);
			pipeItemsCobblestone = createPipe(DefaultProps.PIPE_ITEMS_COBBLESTONE_ID, PipeItemsCobblestone.class, "Cobblestone Transport Pipe", Block.cobblestone, Block.glass, Block.cobblestone);
			pipeItemsStone = createPipe(DefaultProps.PIPE_ITEMS_STONE_ID, PipeItemsStone.class, "Stone Transport Pipe", Block.stone, Block.glass, Block.stone);
			pipeItemsIron = createPipe(DefaultProps.PIPE_ITEMS_IRON_ID, PipeItemsIron.class, "Iron Transport Pipe", Item.ingotIron, Block.glass, Item.ingotIron);
			pipeItemsGold = createPipe(DefaultProps.PIPE_ITEMS_GOLD_ID, PipeItemsGold.class, "Golden Transport Pipe", Item.ingotGold, Block.glass, Item.ingotGold);
			pipeItemsDiamond = createPipe(DefaultProps.PIPE_ITEMS_DIAMOND_ID, PipeItemsDiamond.class, "Diamond Transport Pipe", Item.diamond, Block.glass, Item.diamond);
			pipeItemsObsidian = createPipe(DefaultProps.PIPE_ITEMS_OBSIDIAN_ID, PipeItemsObsidian.class, "Obsidian Transport Pipe", Block.obsidian, Block.glass, Block.obsidian);

			pipeLiquidsWood = createPipe(DefaultProps.PIPE_LIQUIDS_WOOD_ID, PipeLiquidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood, null);
			pipeLiquidsCobblestone = createPipe(DefaultProps.PIPE_LIQUIDS_COBBLESTONE_ID, PipeLiquidsCobblestone.class, "Cobblestone Waterproof Pipe", pipeWaterproof, pipeItemsCobblestone, null);
			pipeLiquidsStone = createPipe(DefaultProps.PIPE_LIQUIDS_STONE_ID, PipeLiquidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone, null);
			pipeLiquidsIron = createPipe(DefaultProps.PIPE_LIQUIDS_IRON_ID, PipeLiquidsIron.class, "Iron Waterproof Pipe", pipeWaterproof, pipeItemsIron, null);
			pipeLiquidsGold = createPipe(DefaultProps.PIPE_LIQUIDS_GOLD_ID, PipeLiquidsGold.class, "Golden Waterproof Pipe", pipeWaterproof, pipeItemsGold, null);
			// diamond
			// obsidian

			pipePowerWood = createPipe(DefaultProps.PIPE_POWER_WOOD_ID, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone, pipeItemsWood, null);
			// cobblestone
			pipePowerStone = createPipe(DefaultProps.PIPE_POWER_STONE_ID, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone, null);
			// iron
			pipePowerGold = createPipe(DefaultProps.PIPE_POWER_GOLD_ID, PipePowerGold.class, "Golden Conductive Pipe", Item.redstone, pipeItemsGold, null);
			// diamond
			// obsidian

			// Fix name and recipe (Structure pipe insteand of Signal?)
			pipeStructureCobblestone = createPipe(DefaultProps.PIPE_STRUCTURE_COBBLESTONE_ID, PipeStructureCobblestone.class, "Cobblestone Structure Pipe", Block.gravel, pipeItemsCobblestone, null);

			// Fix the recipe
			//pipeItemsStipes = createPipe(DefaultProps.PIPE_ITEMS_STRIPES_ID, PipeItemsStripes.class, "Stripes Transport Pipe", new ItemStack(Item.dyePowder, 1, 0), Block.glass, new ItemStack(Item.dyePowder, 1, 11));

			pipeItemsVoid = createPipe(DefaultProps.PIPE_ITEMS_VOID_ID, PipeItemsVoid.class, "Void Transport Pipe", new ItemStack(Item.dyePowder, 1, 0), Block.glass, Item.redstone);

			pipeLiquidsVoid = createPipe(DefaultProps.PIPE_LIQUIDS_VOID_ID, PipeLiquidsVoid.class, "Void Waterproof Pipe", pipeWaterproof, pipeItemsVoid, null);

			pipeItemsSandstone = createPipe(DefaultProps.PIPE_ITEMS_SANDSTONE_ID, PipeItemsSandstone.class, "Sandstone Transport Pipe", Block.sandStone, Block.glass, Block.sandStone);

			pipeLiquidsSandstone = createPipe(DefaultProps.PIPE_LIQUIDS_SANDSTONE_ID, PipeLiquidsSandstone.class, "Sandstone Waterproof Pipe", pipeWaterproof, pipeItemsSandstone, null);
		}
		finally
		{
			BuildCraftCore.mainConfiguration.save();
		}
	}
	@Init
	public void load(FMLInitializationEvent evt) {
		// Register connection handler
		//MinecraftForge.registerConnectionHandler(new ConnectionHandler());

		// Register gui handler
		//MinecraftForge.setGuiHandler(mod_BuildCraftTransport.instance, new GuiHandler());

		TransportProxy.proxy.registerTileEntities();

		// dockingStationBlock = new
		// BlockDockingStation(Integer.parseInt(dockingStationId.value));
		// ModLoader.registerBlock(dockingStationBlock);
		// CoreProxy.addName(dockingStationBlock.setBlockName("dockingStation"),
		// "Docking Station");

		// ModLoader.RegisterTileEntity(TileDockingStation.class,
		// "net.minecraft.src.buildcraft.TileDockingStation");

		for (int j = 0; j < 6; ++j)
		{
			diamondTextures[j] = 1 * 16 + 6 + j;
		}

		redPipeWire = new ItemBuildCraft(DefaultProps.RED_PIPE_WIRE).setIconIndex(4 * 16 + 0);
		redPipeWire.setItemName("redPipeWire");
		LanguageRegistry.addName(redPipeWire, "Red Pipe Wire");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 1),
				new ItemStack(Item.redstone, 1), new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(redPipeWire, 8)));

		bluePipeWire = new ItemBuildCraft(DefaultProps.BLUE_PIPE_WIRE).setIconIndex(4 * 16 + 1);
		bluePipeWire.setItemName("bluePipeWire");
		LanguageRegistry.addName(bluePipeWire, "Blue Pipe Wire");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 4),
				new ItemStack(Item.redstone, 1), new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(bluePipeWire, 8)));

		greenPipeWire = new ItemBuildCraft(DefaultProps.GREEN_PIPE_WIRE).setIconIndex(4 * 16 + 2);
		greenPipeWire.setItemName("greenPipeWire");
		LanguageRegistry.addName(greenPipeWire, "Green Pipe Wire");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 2),
				new ItemStack(Item.redstone, 1), new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(greenPipeWire, 8)));

		yellowPipeWire = new ItemBuildCraft(DefaultProps.YELLOW_PIPE_WIRE).setIconIndex(4 * 16 + 3);
		yellowPipeWire.setItemName("yellowPipeWire");
		LanguageRegistry.addName(yellowPipeWire, "Yellow Pipe Wire");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 11),
				new ItemStack(Item.redstone, 1), new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(yellowPipeWire, 8)));

		pipeGate = new ItemGate(DefaultProps.GATE_ID, 0).setIconIndex(2 * 16 + 3);
		pipeGate.setItemName("pipeGate");

		pipeGateAutarchic = new ItemGate(DefaultProps.GATE_AUTARCHIC_ID, 1).setIconIndex(2 * 16 + 3);
		pipeGateAutarchic.setItemName("pipeGateAutarchic");

		facadeItem = new ItemFacade(DefaultProps.PIPE_FACADE_ID);
		facadeItem.setItemName("pipeFacade");
		ItemFacade.initialize();

		new BptBlockPipe(genericPipeBlock.blockID);

		BuildCraftCore.itemBptProps[pipeItemsWood.shiftedIndex] = new BptItemPipeWodden();
		BuildCraftCore.itemBptProps[pipeLiquidsWood.shiftedIndex] = new BptItemPipeWodden();
		BuildCraftCore.itemBptProps[pipeItemsIron.shiftedIndex] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps[pipeLiquidsIron.shiftedIndex] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps[pipeItemsDiamond.shiftedIndex] = new BptItemPipeDiamond();

		ActionManager.registerTriggerProvider(new PipeTriggerProvider());

		if (BuildCraftCore.loadDefaultRecipes)
		{
			loadRecipes();
		}

		TransportProxy.proxy.registerRenderers();
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());
	}

   public void loadRecipes() {

		// Add base recipe for pipe waterproof.
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Item.dyePowder, 1, 2));

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes)
		{
			if (pipe.isShapeless)
			{
				GameRegistry.addShapelessRecipe(pipe.result, pipe.input);
			}
			else
			{
				GameRegistry.addRecipe(pipe.result, pipe.input);
			}
		}
	}

	private static Item createPipe(int defaultID, Class<? extends Pipe> clas, String descr, Object ingredient1, Object ingredient2, Object ingredient3) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0)) + clas.getSimpleName().substring(1);

		Property prop = BuildCraftCore.mainConfiguration.getOrCreateIntProperty(name + ".id", Configuration.CATEGORY_ITEM, defaultID);

		int id = prop.getInt(defaultID);
		ItemPipe res = BlockGenericPipe.registerPipe(id, clas);
		res.setItemName(clas.getSimpleName());
		LanguageRegistry.addName(res, descr);

		// Add appropriate recipe to temporary list
		PipeRecipe recipe = new PipeRecipe();

		if (ingredient1 != null && ingredient2 != null && ingredient3 != null) {
			recipe.result = new ItemStack(res, 8);
			recipe.input = new Object[] { "   ", "ABC", "   ", Character.valueOf('A'), ingredient1, Character.valueOf('B'), ingredient2, Character.valueOf('C'), ingredient3 };

			pipeRecipes.add(recipe);
		} else if (ingredient1 != null && ingredient2 != null) {
			recipe.isShapeless = true;
			recipe.result = new ItemStack(res, 1);
			recipe.input = new Object[] { ingredient1, ingredient2 };

			pipeRecipes.add(recipe);
		}

		return res;
	}
}
