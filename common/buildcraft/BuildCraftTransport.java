/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.transport.IExtractionHandler;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.GateIconProvider;
import buildcraft.transport.GuiHandler;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemGate;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.ItemPlug;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTriggerProvider;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.WireIconProvider;
import buildcraft.transport.blueprints.BptBlockPipe;
import buildcraft.transport.blueprints.BptItemPipeDiamond;
import buildcraft.transport.blueprints.BptItemPipeEmerald;
import buildcraft.transport.blueprints.BptItemPipeIron;
import buildcraft.transport.blueprints.BptItemPipeWooden;
import buildcraft.transport.network.PacketHandlerTransport;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsGold;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsSandstone;
import buildcraft.transport.pipes.PipeItemsStone;
import buildcraft.transport.pipes.PipeItemsVoid;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipeLiquidsCobblestone;
import buildcraft.transport.pipes.PipeLiquidsEmerald;
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
import buildcraft.transport.triggers.ActionEnergyPulser;
import buildcraft.transport.triggers.ActionSignalOutput;
import buildcraft.transport.triggers.ActionSingleEnergyPulse;
import buildcraft.transport.triggers.TriggerPipeContents;
import buildcraft.transport.triggers.TriggerPipeContents.Kind;
import buildcraft.transport.triggers.TriggerPipeSignal;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.IMCCallback;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(version = Version.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = { DefaultProps.NET_CHANNEL_NAME }, packetHandler = PacketHandlerTransport.class)
public class BuildCraftTransport {
	public static BlockGenericPipe genericPipeBlock;

	public static boolean usePipeLoss;
	public static int maxItemsInPipes;
	public static float pipeDurability;

	public static Item pipeWaterproof;
	public static Item pipeGate;
	public static Item pipeGateAutarchic;
	public static Item redPipeWire;
	public static Item bluePipeWire;
	public static Item greenPipeWire;
	public static Item yellowPipeWire;

	public static Item pipeItemsWood;
	public static Item pipeItemsEmerald;
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
	public static Item pipeLiquidsEmerald;

	public static Item pipePowerWood;
	public static Item pipePowerStone;
	public static Item pipePowerGold;

	public static Item facadeItem;
	public static Item plugItem;

	// public static Item pipeItemsStipes;
	public static Item pipeStructureCobblestone;
	public static int groupItemsTrigger;

	public static BCTrigger triggerPipeEmpty = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_EMPTY, Kind.Empty);
	public static BCTrigger triggerPipeItems = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_ITEMS, Kind.ContainsItems);
	public static BCTrigger triggerPipeLiquids = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_LIQUIDS, Kind.ContainsLiquids);
	public static BCTrigger triggerPipeEnergy = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_ENERGY, Kind.ContainsEnergy);
	public static BCTrigger triggerRedSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_RED_SIGNAL_ACTIVE, true, IPipe.WireColor.Red);
	public static BCTrigger triggerRedSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_RED_SIGNAL_INACTIVE, false, IPipe.WireColor.Red);
	public static BCTrigger triggerBlueSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_BLUE_SIGNAL_ACTIVE, true, IPipe.WireColor.Blue);
	public static BCTrigger triggerBlueSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_BLUE_SIGNAL_INACTIVE, false, IPipe.WireColor.Blue);
	public static BCTrigger triggerGreenSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_GREEN_SIGNAL_ACTIVE, true, IPipe.WireColor.Green);
	public static BCTrigger triggerGreenSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_GREEN_SIGNAL_INACTIVE, false, IPipe.WireColor.Green);
	public static BCTrigger triggerYellowSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_YELLOW_SIGNAL_ACTIVE, true, IPipe.WireColor.Yellow);
	public static BCTrigger triggerYellowSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_YELLOW_SIGNAL_INACTIVE, false, IPipe.WireColor.Yellow);

	public static BCAction actionRedSignal = new ActionSignalOutput(DefaultProps.ACTION_RED_SIGNAL, IPipe.WireColor.Red);
	public static BCAction actionBlueSignal = new ActionSignalOutput(DefaultProps.ACTION_BLUE_SIGNAL, IPipe.WireColor.Blue);
	public static BCAction actionGreenSignal = new ActionSignalOutput(DefaultProps.ACTION_GREEN_SIGNAL, IPipe.WireColor.Green);
	public static BCAction actionYellowSignal = new ActionSignalOutput(DefaultProps.ACTION_YELLOW_SIGNAL, IPipe.WireColor.Yellow);
	public static BCAction actionEnergyPulser = new ActionEnergyPulser(DefaultProps.ACTION_ENERGY_PULSER);
	public static BCAction actionSingleEnergyPulse = new ActionSingleEnergyPulse(DefaultProps.ACTION_SINGLE_ENERGY_PULSE);

	@Instance("BuildCraft|Transport")
	public static BuildCraftTransport instance;
	
	public IIconProvider pipeIconProvider = new PipeIconProvider();
	public IIconProvider gateIconProvider = new GateIconProvider();
	public IIconProvider wireIconProvider = new WireIconProvider();

	private static class PipeRecipe {
		boolean isShapeless = false; // pipe recipes come shaped and unshaped.
		ItemStack result;
		Object[] input;
	}

	private static class ExtractionHandler implements IExtractionHandler {
		private final String[] items;
		private final String[] liquids;

		public ExtractionHandler(String[] items, String[] liquids) {
			this.items = items;
			this.liquids = liquids;
		}

		@Override
		public boolean canExtractItems(Object extractor, World world, int i, int j, int k) {
			return testStrings(items, world, i, j, k);
		}

		@Override
		public boolean canExtractLiquids(Object extractor, World world, int i, int j, int k) {
			return testStrings(liquids, world, i, j, k);
		}

		private boolean testStrings(String[] excludedBlocks, World world, int i, int j, int k) {
			int id = world.getBlockId(i, j, k);
			Block block = Block.blocksList[id];
			if (block == null)
				return false;

			int meta = world.getBlockMetadata(i, j, k);

			for (String excluded : excludedBlocks) {
				if (excluded.equals(block.getUnlocalizedName()))
					return false;

				String[] tokens = excluded.split(":");
				if (tokens[0].equals(Integer.toString(id)) && (tokens.length == 1 || tokens[1].equals(Integer.toString(meta))))
					return false;
			}
			return true;
		}
	}

	private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();

	@PreInit
	public void preInitialize(FMLPreInitializationEvent evt) {
		try {
			Property pipeLoss = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "power.usePipeLoss", DefaultProps.USE_PIPELOSS);
			pipeLoss.comment = "Set to false to turn off energy loss over distance on all power pipes";
			usePipeLoss = pipeLoss.getBoolean(DefaultProps.USE_PIPELOSS);

			Property durability = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.durability", DefaultProps.PIPES_DURABILITY);
			durability.comment = "How long a pipe will take to break";
			pipeDurability = (float) durability.getDouble(DefaultProps.PIPES_DURABILITY);

			Property exclusionItemList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_BLOCK, "woodenPipe.item.exclusion", new String[0]);

			String[] excludedItemBlocks = exclusionItemList.getStringList();
			if(excludedItemBlocks != null) {
				for (int j = 0; j < excludedItemBlocks.length; ++j) {
					excludedItemBlocks[j] = excludedItemBlocks[j].trim();
				}
			} else
				excludedItemBlocks = new String[0];

			Property exclusionLiquidList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_BLOCK, "woodenPipe.liquid.exclusion", new String[0]);

			String[] excludedLiquidBlocks = exclusionLiquidList.getStringList();
			if(excludedLiquidBlocks != null) {
				for (int j = 0; j < excludedLiquidBlocks.length; ++j) {
					excludedLiquidBlocks[j] = excludedLiquidBlocks[j].trim();
				}
			} else
				excludedLiquidBlocks = new String[0];

			PipeManager.registerExtractionHandler(new ExtractionHandler(excludedItemBlocks, excludedLiquidBlocks));

			Property maxItemInPipesProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.maxItems", 100);
			maxItemInPipesProp.comment = "pipes containing more than this amount of items will explode, not dropping any item";

			maxItemsInPipes = maxItemInPipesProp.getInt();

			Property groupItemsTriggerProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.groupItemsTrigger", 32);
			groupItemsTriggerProp.comment = "when reaching this amount of objects in a pipes, items will be automatically grouped";

			groupItemsTrigger = groupItemsTriggerProp.getInt();


			Property genericPipeId = BuildCraftCore.mainConfiguration.getBlock("pipe.id", DefaultProps.GENERIC_PIPE_ID);

			Property pipeWaterproofId = BuildCraftCore.mainConfiguration.getItem("pipeWaterproof.id", DefaultProps.PIPE_WATERPROOF_ID);

			pipeWaterproof = new ItemBuildCraft(pipeWaterproofId.getInt());
			pipeWaterproof.setUnlocalizedName("pipeWaterproof");
			pipeWaterproof.setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
			LanguageRegistry.addName(pipeWaterproof, "Pipe Waterproof");
			genericPipeBlock = new BlockGenericPipe(genericPipeId.getInt());
			GameRegistry.registerBlock(genericPipeBlock);

			// Fixing retro-compatiblity
			pipeItemsWood = createPipe(DefaultProps.PIPE_ITEMS_WOOD_ID, PipeItemsWood.class, "Wooden Transport Pipe", "plankWood", Block.glass, "plankWood");
			pipeItemsEmerald = createPipe(DefaultProps.PIPE_ITEMS_EMERALD_ID, PipeItemsEmerald.class, "Emerald Transport Pipe", Item.emerald, Block.glass, Item.emerald);
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
			pipeLiquidsEmerald = createPipe(DefaultProps.PIPE_LIQUIDS_EMERALD_ID, PipeLiquidsEmerald.class, "Emerald Waterproof Pipe", pipeWaterproof, pipeItemsEmerald, null);

			pipePowerWood = createPipe(DefaultProps.PIPE_POWER_WOOD_ID, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone, pipeItemsWood, null);
			pipePowerStone = createPipe(DefaultProps.PIPE_POWER_STONE_ID, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone, null);
			pipePowerGold = createPipe(DefaultProps.PIPE_POWER_GOLD_ID, PipePowerGold.class, "Golden Conductive Pipe", Item.redstone, pipeItemsGold, null);

			pipeStructureCobblestone = createPipe(DefaultProps.PIPE_STRUCTURE_COBBLESTONE_ID, PipeStructureCobblestone.class, "Cobblestone Structure Pipe", Block.gravel, pipeItemsCobblestone, null);

			// Fix the recipe
			// pipeItemsStipes = createPipe(DefaultProps.PIPE_ITEMS_STRIPES_ID, PipeItemsStripes.class, "Stripes Transport Pipe", new ItemStack(Item.dyePowder,
			// 1, 0), Block.glass, new ItemStack(Item.dyePowder, 1, 11));

			pipeItemsVoid = createPipe(DefaultProps.PIPE_ITEMS_VOID_ID, PipeItemsVoid.class, "Void Transport Pipe", new ItemStack(Item.dyePowder, 1, 0),
					Block.glass, Item.redstone);

			pipeLiquidsVoid = createPipe(DefaultProps.PIPE_LIQUIDS_VOID_ID, PipeLiquidsVoid.class, "Void Waterproof Pipe", pipeWaterproof, pipeItemsVoid, null);

			pipeItemsSandstone = createPipe(DefaultProps.PIPE_ITEMS_SANDSTONE_ID, PipeItemsSandstone.class, "Sandstone Transport Pipe", Block.sandStone,
					Block.glass, Block.sandStone);

			pipeLiquidsSandstone = createPipe(DefaultProps.PIPE_LIQUIDS_SANDSTONE_ID, PipeLiquidsSandstone.class, "Sandstone Waterproof Pipe", pipeWaterproof,
					pipeItemsSandstone, null);

			Property redPipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "redPipeWire.id", DefaultProps.RED_PIPE_WIRE);
			redPipeWire = new ItemBuildCraft(redPipeWireId.getInt());
			redPipeWire.setUnlocalizedName("redPipeWire");
			LanguageRegistry.addName(redPipeWire, "Red Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 1), new ItemStack(Item.redstone, 1),
					new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(redPipeWire, 8)));

			Property bluePipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "bluePipeWire.id", DefaultProps.BLUE_PIPE_WIRE);
			bluePipeWire = new ItemBuildCraft(bluePipeWireId.getInt());
			bluePipeWire.setUnlocalizedName("bluePipeWire");
			LanguageRegistry.addName(bluePipeWire, "Blue Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 4), new ItemStack(Item.redstone, 1),
					new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(bluePipeWire, 8)));

			Property greenPipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "greenPipeWire.id", DefaultProps.GREEN_PIPE_WIRE);
			greenPipeWire = new ItemBuildCraft(greenPipeWireId.getInt());
			greenPipeWire.setUnlocalizedName("greenPipeWire");
			LanguageRegistry.addName(greenPipeWire, "Green Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 2), new ItemStack(Item.redstone, 1),
					new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(greenPipeWire, 8)));

			Property yellowPipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "yellowPipeWire.id", DefaultProps.YELLOW_PIPE_WIRE);
			yellowPipeWire = new ItemBuildCraft(yellowPipeWireId.getInt());
			yellowPipeWire.setUnlocalizedName("yellowPipeWire");
			LanguageRegistry.addName(yellowPipeWire, "Yellow Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.dyePowder, 1, 11), new ItemStack(Item.redstone, 1),
					new ItemStack(Item.ingotIron, 1) }, 500, new ItemStack(yellowPipeWire, 8)));

			Property pipeGateId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipeGate.id", DefaultProps.GATE_ID);
			pipeGate = new ItemGate(pipeGateId.getInt(), 0);
			pipeGate.setUnlocalizedName("pipeGate");

			Property pipeGateAutarchicId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipeGateAutarchic.id",
					DefaultProps.GATE_AUTARCHIC_ID);
			pipeGateAutarchic = new ItemGate(pipeGateAutarchicId.getInt(), 1);
			pipeGateAutarchic.setUnlocalizedName("pipeGateAutarchic");

			Property pipeFacadeId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipeFacade.id", DefaultProps.PIPE_FACADE_ID);
			facadeItem = new ItemFacade(pipeFacadeId.getInt());
			facadeItem.setUnlocalizedName("pipeFacade");
			
			Property pipePlugId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipePlug.id", DefaultProps.PIPE_PLUG_ID);
			plugItem = new ItemPlug(pipePlugId.getInt());
			plugItem.setUnlocalizedName("pipePlug");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {new ItemStack(pipeStructureCobblestone)}, 1000, new ItemStack(plugItem, 8)));

		} finally {
			BuildCraftCore.mainConfiguration.save();
		}
	}

	@Init
	public void load(FMLInitializationEvent evt) {
		// Register connection handler
		// MinecraftForge.registerConnectionHandler(new ConnectionHandler());

		// Register gui handler
		// MinecraftForge.setGuiHandler(mod_BuildCraftTransport.instance, new GuiHandler());

		TransportProxy.proxy.registerTileEntities();

		// dockingStationBlock = new
		// BlockDockingStation(Integer.parseInt(dockingStationId.value));
		// ModLoader.registerBlock(dockingStationBlock);
		// CoreProxy.addName(dockingStationBlock.setBlockName("dockingStation"),
		// "Docking Station");

		// ModLoader.RegisterTileEntity(TileDockingStation.class,
		// "net.minecraft.src.buildcraft.TileDockingStation");

		new BptBlockPipe(genericPipeBlock.blockID);

		BuildCraftCore.itemBptProps[pipeItemsWood.itemID] = new BptItemPipeWooden();
		BuildCraftCore.itemBptProps[pipeLiquidsWood.itemID] = new BptItemPipeWooden();
		BuildCraftCore.itemBptProps[pipeItemsIron.itemID] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps[pipeLiquidsIron.itemID] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps[pipeItemsDiamond.itemID] = new BptItemPipeDiamond();
		BuildCraftCore.itemBptProps[pipeItemsEmerald.itemID] = new BptItemPipeEmerald();

		ActionManager.registerTriggerProvider(new PipeTriggerProvider());

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		TransportProxy.proxy.registerRenderers();
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent evt) {
		ItemFacade.initialize();
	}

	public void loadRecipes() {

		// Add base recipe for pipe waterproof.
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Item.dyePowder, 1, 2));

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes) {
			if (pipe.isShapeless) {
				GameRegistry.addShapelessRecipe(pipe.result, pipe.input);
			} else {
				CoreProxy.proxy.addCraftingRecipe(pipe.result, pipe.input);
			}
		}
	}

	@IMCCallback
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		Splitter splitter = Splitter.on("@").trimResults();
		for (IMCMessage m : event.getMessages()) {
			if ("add-facade".equals(m.key)) {
				String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
				if (array.length != 2) {
					Logger.getLogger("Buildcraft").log(Level.INFO,
							String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
					continue;
				}
				Integer blId = Ints.tryParse(array[0]);
				Integer metaId = Ints.tryParse(array[1]);
				if (blId == null || metaId == null) {
					Logger.getLogger("Buildcraft").log(Level.INFO,
							String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
					continue;
				}
				ItemFacade.addFacade(new ItemStack(blId, 1, metaId));
			}
		}
	}

	public static Item createPipe(int defaultID, Class<? extends Pipe> clas, String descr, Object ingredient1, Object ingredient2, Object ingredient3) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0)) + clas.getSimpleName().substring(1);

		Property prop = BuildCraftCore.mainConfiguration.getItem(name + ".id", defaultID);

		int id = prop.getInt(defaultID);
		ItemPipe res = BlockGenericPipe.registerPipe(id, clas);
		res.setUnlocalizedName(clas.getSimpleName());
		LanguageRegistry.addName(res, descr);

		// Add appropriate recipe to temporary list
		PipeRecipe recipe = new PipeRecipe();

		if (ingredient1 != null && ingredient2 != null && ingredient3 != null) {
			recipe.result = new ItemStack(res, 8);
			recipe.input = new Object[] { "ABC", Character.valueOf('A'), ingredient1, Character.valueOf('B'), ingredient2, Character.valueOf('C'), ingredient3 };

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
