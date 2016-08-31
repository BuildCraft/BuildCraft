/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.transport.IExtractionHandler;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.EnumColor;
import buildcraft.transport.BlockFilteredBuffer;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.GuiHandler;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.ItemPlug;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTriggerProvider;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.WireIconProvider;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.transport.PipeWire;
import buildcraft.transport.ItemPipeWire;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.network.PacketHandlerTransport;
import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeFluidsGold;
import buildcraft.transport.pipes.PipeFluidsIron;
import buildcraft.transport.pipes.PipeFluidsSandstone;
import buildcraft.transport.pipes.PipeFluidsStone;
import buildcraft.transport.pipes.PipeFluidsVoid;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsDaizuli;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsGold;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsEmzuli;
import buildcraft.transport.pipes.PipeItemsLapis;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsQuartz;
import buildcraft.transport.pipes.PipeItemsSandstone;
import buildcraft.transport.pipes.PipeItemsStone;
import buildcraft.transport.pipes.PipeItemsVoid;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerIron;
import buildcraft.transport.pipes.PipePowerIron.PowerMode;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.pipes.PipeStructureCobblestone;
import buildcraft.transport.triggers.ActionEnergyPulsar;
import buildcraft.transport.triggers.ActionExtractionPreset;
import buildcraft.transport.triggers.ActionPipeColor;
import buildcraft.transport.triggers.ActionPipeDirection;
import buildcraft.transport.triggers.ActionPowerLimiter;
import buildcraft.transport.triggers.ActionSignalOutput;
import buildcraft.transport.triggers.ActionSingleEnergyPulse;
import buildcraft.transport.triggers.TriggerPipeContents;
import buildcraft.transport.triggers.TriggerClockTimer;
import buildcraft.transport.triggers.TriggerClockTimer.Time;
import buildcraft.transport.triggers.TriggerPipeContents.PipeContents;
import buildcraft.transport.triggers.TriggerPipeSignal;
import buildcraft.transport.triggers.TriggerRedstoneFaderInput;
import buildcraft.transport.triggers.ActionRedstoneFaderOutput;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.RecipeSorter;

@Mod(version = Version.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTransport extends BuildCraftMod {

	public static BlockGenericPipe genericPipeBlock;
	public static float pipeDurability;
	public static Item pipeWaterproof;
	public static Item pipeGate;
	public static Item pipeWire;
	public static Item pipeItemsWood;
	public static Item pipeItemsEmerald;
	public static Item pipeItemsStone;
	public static Item pipeItemsCobblestone;
	public static Item pipeItemsIron;
	public static Item pipeItemsQuartz;
	public static Item pipeItemsGold;
	public static Item pipeItemsDiamond;
	public static Item pipeItemsObsidian;
	public static Item pipeItemsLapis;
	public static Item pipeItemsDaizuli;
	public static Item pipeItemsVoid;
	public static Item pipeItemsSandstone;
	public static Item pipeItemsEmzuli;
	public static Item pipeFluidsWood;
	public static Item pipeFluidsCobblestone;
	public static Item pipeFluidsStone;
	public static Item pipeFluidsIron;
	public static Item pipeFluidsGold;
	public static Item pipeFluidsVoid;
	public static Item pipeFluidsSandstone;
	public static Item pipeFluidsEmerald;
	public static Item pipePowerWood;
	public static Item pipePowerCobblestone;
	public static Item pipePowerStone;
	public static Item pipePowerQuartz;
	public static Item pipePowerIron;
	public static Item pipePowerGold;
	public static Item pipePowerDiamond;
	public static ItemFacade facadeItem;
	public static Item plugItem;
	public static BlockFilteredBuffer filteredBufferBlock;
	// public static Item pipeItemsStipes;
	public static Item pipeStructureCobblestone;
	public static int groupItemsTrigger;
	public static String[] facadeBlacklist;

	public static BCTrigger[] triggerPipe = new BCTrigger[PipeContents.values().length];
	public static BCTrigger[] triggerPipeWireActive = new BCTrigger[PipeWire.values().length];
	public static BCTrigger[] triggerPipeWireInactive = new BCTrigger[PipeWire.values().length];
	public static BCTrigger[] triggerTimer = new BCTrigger[TriggerClockTimer.Time.VALUES.length];
	public static BCTrigger[] triggerRedstoneLevel = new BCTrigger[15];
	public static BCAction[] actionPipeWire = new ActionSignalOutput[PipeWire.values().length];
	public static BCAction actionEnergyPulser = new ActionEnergyPulsar();
	public static BCAction actionSingleEnergyPulse = new ActionSingleEnergyPulse();
	public static BCAction[] actionPipeColor = new BCAction[16];
	public static BCAction[] actionPipeDirection = new BCAction[16];
	public static BCAction[] actionPowerLimiter = new BCAction[7];
	public static BCAction[] actionRedstoneLevel = new BCAction[15];
	public static BCAction actionExtractionPresetRed = new ActionExtractionPreset(EnumColor.RED);
	public static BCAction actionExtractionPresetBlue = new ActionExtractionPreset(EnumColor.BLUE);
	public static BCAction actionExtractionPresetGreen = new ActionExtractionPreset(EnumColor.GREEN);
	public static BCAction actionExtractionPresetYellow = new ActionExtractionPreset(EnumColor.YELLOW);
	public IIconProvider pipeIconProvider = new PipeIconProvider();
	public IIconProvider wireIconProvider = new WireIconProvider();

	@Instance("BuildCraft|Transport")
	public static BuildCraftTransport instance;

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
		public boolean canExtractFluids(Object extractor, World world, int i, int j, int k) {
			return testStrings(liquids, world, i, j, k);
		}

		private boolean testStrings(String[] excludedBlocks, World world, int i, int j, int k) {			
			Block block = world.getBlock(i, j, k);
			if (block == null)
				return false;

			int meta = world.getBlockMetadata(i, j, k);

			// TODO: the exculded list is not taken into account. This probably
			// needs to be migrated to an implementation based on names instead
			// of ids, low priority for now.
			/*for (String excluded : excludedBlocks) {
				if (excluded.equals(block.getUnlocalizedName()))
					return false;

				String[] tokens = excluded.split(":");
				if (tokens[0].equals(Integer.toString(id)) && (tokens.length == 1 || tokens[1].equals(Integer.toString(meta))))
					return false;
			}*/
			return true;
		}
	}
	private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		try {
			Property durability = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.durability", DefaultProps.PIPES_DURABILITY);
			durability.comment = "How long a pipe will take to break";
			pipeDurability = (float) durability.getDouble(DefaultProps.PIPES_DURABILITY);

			Property exclusionItemList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "woodenPipe.item.exclusion", new String[0]);

			String[] excludedItemBlocks = exclusionItemList.getStringList();
			if (excludedItemBlocks != null) {
				for (int j = 0; j < excludedItemBlocks.length; ++j) {
					excludedItemBlocks[j] = excludedItemBlocks[j].trim();
				}
			} else
				excludedItemBlocks = new String[0];

			Property exclusionFluidList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "woodenPipe.liquid.exclusion", new String[0]);

			String[] excludedFluidBlocks = exclusionFluidList.getStringList();
			if (excludedFluidBlocks != null) {
				for (int j = 0; j < excludedFluidBlocks.length; ++j) {
					excludedFluidBlocks[j] = excludedFluidBlocks[j].trim();
				}
			} else
				excludedFluidBlocks = new String[0];

			filteredBufferBlock = new BlockFilteredBuffer();
			CoreProxy.proxy.registerBlock(filteredBufferBlock.setBlockName("filteredBufferBlock"));

			PipeManager.registerExtractionHandler(new ExtractionHandler(excludedItemBlocks, excludedFluidBlocks));

			GateExpansions.registerExpansion(GateExpansionPulsar.INSTANCE);
			GateExpansions.registerExpansion(GateExpansionTimer.INSTANCE);
			GateExpansions.registerExpansion(GateExpansionRedstoneFader.INSTANCE);

			Property groupItemsTriggerProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.groupItemsTrigger", 32);
			groupItemsTriggerProp.comment = "when reaching this amount of objects in a pipes, items will be automatically grouped";
			groupItemsTrigger = groupItemsTriggerProp.getInt();

			Property facadeBlacklistProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "facade.blacklist", new String[] {
					Block.blockRegistry.getNameForObject(Blocks.bedrock),
					Block.blockRegistry.getNameForObject(Blocks.command_block),
					Block.blockRegistry.getNameForObject(Blocks.end_portal_frame),
					Block.blockRegistry.getNameForObject(Blocks.grass),
					Block.blockRegistry.getNameForObject(Blocks.lit_pumpkin),
					Block.blockRegistry.getNameForObject(Blocks.lit_redstone_lamp),
					Block.blockRegistry.getNameForObject(Blocks.mob_spawner),
					Block.blockRegistry.getNameForObject(Blocks.monster_egg),
					Block.blockRegistry.getNameForObject(Blocks.redstone_lamp),
					Block.blockRegistry.getNameForObject(Blocks.double_stone_slab),
					Block.blockRegistry.getNameForObject(Blocks.double_wooden_slab),
					Block.blockRegistry.getNameForObject(Blocks.sponge),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.architectBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.builderBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.fillerBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.libraryBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.autoWorkbenchBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.floodGateBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.miningWellBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.pumpBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.quarryBlock)),
					BuildCraftConfiguration.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftTransport.filteredBufferBlock)),
			});

			facadeBlacklistProp.comment = "Blocks listed here will not have facades created. The format is modid:blockname.\nFor mods with a | character, the value needs to be surrounded with quotes.";
			facadeBlacklist = facadeBlacklistProp.getStringList();

			pipeWaterproof = new ItemBuildCraft();
			pipeWaterproof.setUnlocalizedName("pipeWaterproof");
			LanguageRegistry.addName(pipeWaterproof, "Pipe Sealant");
			CoreProxy.proxy.registerItem(pipeWaterproof);

			genericPipeBlock = new BlockGenericPipe();
			CoreProxy.proxy.registerBlock(genericPipeBlock.setBlockName("pipeBlock"), ItemBlock.class);

			pipeItemsWood = buildPipe(DefaultProps.PIPE_ITEMS_WOOD_ID, PipeItemsWood.class, "Wooden Transport Pipe", "plankWood", Blocks.glass, "plankWood");
			pipeItemsEmerald = buildPipe(DefaultProps.PIPE_ITEMS_EMERALD_ID, PipeItemsEmerald.class, "Emerald Transport Pipe", Items.emerald, Blocks.glass, Items.emerald);
			pipeItemsCobblestone = buildPipe(DefaultProps.PIPE_ITEMS_COBBLESTONE_ID, PipeItemsCobblestone.class, "Cobblestone Transport Pipe", "cobblestone", Blocks.glass, "cobblestone");
			pipeItemsStone = buildPipe(DefaultProps.PIPE_ITEMS_STONE_ID, PipeItemsStone.class, "Stone Transport Pipe", "stone", Blocks.glass, "stone");
			pipeItemsQuartz = buildPipe(DefaultProps.PIPE_ITEMS_QUARTZ_ID, PipeItemsQuartz.class, "Quartz Transport Pipe", Blocks.quartz_block, Blocks.glass, Blocks.quartz_block);
			pipeItemsIron = buildPipe(DefaultProps.PIPE_ITEMS_IRON_ID, PipeItemsIron.class, "Iron Transport Pipe", Items.iron_ingot, Blocks.glass, Items.iron_ingot);
			pipeItemsGold = buildPipe(DefaultProps.PIPE_ITEMS_GOLD_ID, PipeItemsGold.class, "Golden Transport Pipe", Items.gold_ingot, Blocks.glass, Items.gold_ingot);
			pipeItemsDiamond = buildPipe(DefaultProps.PIPE_ITEMS_DIAMOND_ID, PipeItemsDiamond.class, "Diamond Transport Pipe", Items.diamond, Blocks.glass, Items.diamond);
			pipeItemsObsidian = buildPipe(DefaultProps.PIPE_ITEMS_OBSIDIAN_ID, PipeItemsObsidian.class, "Obsidian Transport Pipe", Blocks.obsidian, Blocks.glass, Blocks.obsidian);
			pipeItemsLapis = buildPipe(DefaultProps.PIPE_ITEMS_LAPIS_ID, PipeItemsLapis.class, "Lapis Transport Pipe", Blocks.lapis_block, Blocks.glass, Blocks.lapis_block);
			pipeItemsDaizuli = buildPipe(DefaultProps.PIPE_ITEMS_DAIZULI_ID, PipeItemsDaizuli.class, "Daizuli Transport Pipe", Blocks.lapis_block, Blocks.glass, Items.diamond);
			pipeItemsSandstone = buildPipe(DefaultProps.PIPE_ITEMS_SANDSTONE_ID, PipeItemsSandstone.class, "Sandstone Transport Pipe", Blocks.sandstone, Blocks.glass, Blocks.sandstone);
			pipeItemsVoid = buildPipe(DefaultProps.PIPE_ITEMS_VOID_ID, PipeItemsVoid.class, "Void Transport Pipe", "dyeBlack", Blocks.glass, Items.redstone);
			pipeItemsEmzuli = buildPipe(DefaultProps.PIPE_ITEMS_EMZULI_ID, PipeItemsEmzuli.class, "Emzuli Transport Pipe", Blocks.lapis_block, Blocks.glass, Items.emerald);

			pipeFluidsWood = buildPipe(DefaultProps.PIPE_LIQUIDS_WOOD_ID, PipeFluidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood);
			pipeFluidsCobblestone = buildPipe(DefaultProps.PIPE_LIQUIDS_COBBLESTONE_ID, PipeFluidsCobblestone.class, "Cobblestone Waterproof Pipe", pipeWaterproof, pipeItemsCobblestone);
			pipeFluidsStone = buildPipe(DefaultProps.PIPE_LIQUIDS_STONE_ID, PipeFluidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone);
			pipeFluidsIron = buildPipe(DefaultProps.PIPE_LIQUIDS_IRON_ID, PipeFluidsIron.class, "Iron Waterproof Pipe", pipeWaterproof, pipeItemsIron);
			pipeFluidsGold = buildPipe(DefaultProps.PIPE_LIQUIDS_GOLD_ID, PipeFluidsGold.class, "Golden Waterproof Pipe", pipeWaterproof, pipeItemsGold);
			pipeFluidsEmerald = buildPipe(DefaultProps.PIPE_LIQUIDS_EMERALD_ID, PipeFluidsEmerald.class, "Emerald Waterproof Pipe", pipeWaterproof, pipeItemsEmerald);
			pipeFluidsSandstone = buildPipe(DefaultProps.PIPE_LIQUIDS_SANDSTONE_ID, PipeFluidsSandstone.class, "Sandstone Waterproof Pipe", pipeWaterproof, pipeItemsSandstone);
			pipeFluidsVoid = buildPipe(DefaultProps.PIPE_LIQUIDS_VOID_ID, PipeFluidsVoid.class, "Void Waterproof Pipe", pipeWaterproof, pipeItemsVoid);

			pipePowerWood = buildPipe(DefaultProps.PIPE_POWER_WOOD_ID, PipePowerWood.class, "Wooden Kinesis Pipe", Items.redstone, pipeItemsWood);
			pipePowerCobblestone = buildPipe(DefaultProps.PIPE_POWER_COBBLESTONE_ID, PipePowerCobblestone.class, "Cobblestone Kinesis Pipe", Items.redstone, pipeItemsCobblestone);
			pipePowerStone = buildPipe(DefaultProps.PIPE_POWER_STONE_ID, PipePowerStone.class, "Stone Kinesis Pipe", Items.redstone, pipeItemsStone);
			pipePowerQuartz = buildPipe(DefaultProps.PIPE_POWER_QUARTZ_ID, PipePowerQuartz.class, "Quartz Kinesis Pipe", Items.redstone, pipeItemsQuartz);
			pipePowerIron = buildPipe(DefaultProps.PIPE_POWER_IRON_ID, PipePowerIron.class, "Iron Kinesis Pipe", Items.redstone, pipeItemsIron);
			pipePowerGold = buildPipe(DefaultProps.PIPE_POWER_GOLD_ID, PipePowerGold.class, "Golden Kinesis Pipe", Items.redstone, pipeItemsGold);
			pipePowerDiamond = buildPipe(DefaultProps.PIPE_POWER_DIAMOND_ID, PipePowerDiamond.class, "Diamond Kinesis Pipe", Items.redstone, pipeItemsDiamond);

			pipeStructureCobblestone = buildPipe(DefaultProps.PIPE_STRUCTURE_COBBLESTONE_ID, PipeStructureCobblestone.class, "Cobblestone Structure Pipe", Blocks.gravel, pipeItemsCobblestone);

			// Fix the recipe
			// pipeItemsStipes = createPipe(DefaultProps.PIPE_ITEMS_STRIPES_ID, PipeItemsStripes.class, "Stripes Transport Pipe", new ItemStack(Item.dyePowder,
			// 1, 0), Block.glass, new ItemStack(Item.dyePowder, 1, 11));

			pipeWire = new ItemPipeWire();
			LanguageRegistry.addName(pipeWire, "Pipe Wire");
			CoreProxy.proxy.registerItem(pipeWire);
			PipeWire.item = pipeWire;

			pipeGate = new ItemGate();
			pipeGate.setUnlocalizedName("pipeGate");
			CoreProxy.proxy.registerItem(pipeGate);

			facadeItem = new ItemFacade();
			facadeItem.setUnlocalizedName("pipeFacade");
			CoreProxy.proxy.registerItem(facadeItem);

			plugItem = new ItemPlug();
			plugItem.setUnlocalizedName("pipePlug");
			CoreProxy.proxy.registerItem(plugItem);

			for (PipeContents kind : PipeContents.values()) {
				triggerPipe[kind.ordinal()] = new TriggerPipeContents(kind);
			}
			
			for (PipeWire wire : PipeWire.values()) {
				triggerPipeWireActive[wire.ordinal()] = new TriggerPipeSignal(true, wire);
				triggerPipeWireInactive[wire.ordinal()] = new TriggerPipeSignal(false, wire);
				actionPipeWire[wire.ordinal()] = new ActionSignalOutput(wire);
			}

			for (Time time : TriggerClockTimer.Time.VALUES) {
				triggerTimer[time.ordinal()] = new TriggerClockTimer(time);
			}

			for (int level = 0; level < triggerRedstoneLevel.length; level++) {
				triggerRedstoneLevel[level] = new TriggerRedstoneFaderInput(level + 1);
				actionRedstoneLevel[level] = new ActionRedstoneFaderOutput(level + 1);
			}

			for (EnumColor color : EnumColor.VALUES) {
				actionPipeColor[color.ordinal()] = new ActionPipeColor(color);
			}

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				actionPipeDirection[direction.ordinal()] = new ActionPipeDirection(direction);
			}

			for (PowerMode limit : PowerMode.VALUES) {
				actionPowerLimiter[limit.ordinal()] = new ActionPowerLimiter(limit);
			}
		} finally {
			BuildCraftCore.mainConfiguration.save();
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-TRANSPORT", new BuildCraftChannelHandler(), new PacketHandlerTransport());
		
		// Register connection handler
		// MinecraftForge.registerConnectionHandler(new ConnectionHandler());

		// Register GUI handler
		// MinecraftForge.setGuiHandler(mod_BuildCraftTransport.instance, new GuiHandler());

		TransportProxy.proxy.registerTileEntities();

		// dockingStationBlock = new
		// BlockDockingStation(Integer.parseInt(dockingStationId.value));
		// ModLoader.registerBlock(dockingStationBlock);
		// CoreProxy.addName(dockingStationBlock.setBlockName("dockingStation"),
		// "Docking Station");

		// ModLoader.RegisterTileEntity(TileDockingStation.class,
		// "net.minecraft.src.buildcraft.TileDockingStation");

		//new BptBlockPipe(genericPipeBlock.blockID);

		//BuildCraftCore.itemBptProps[pipeItemsWood.itemID] = new BptItemPipeWooden();
		//BuildCraftCore.itemBptProps[pipeFluidsWood.itemID] = new BptItemPipeWooden();
		//BuildCraftCore.itemBptProps[pipeItemsIron.itemID] = new BptItemPipeIron();
		//BuildCraftCore.itemBptProps[pipeFluidsIron.itemID] = new BptItemPipeIron();
		//BuildCraftCore.itemBptProps[pipeItemsDiamond.itemID] = new BptItemPipeDiamond();
		//BuildCraftCore.itemBptProps[pipeItemsEmerald.itemID] = new BptItemPipeEmerald();

		ActionManager.registerTriggerProvider(new PipeTriggerProvider());

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		TransportProxy.proxy.registerRenderers();
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		ItemFacade.initialize();
	}

	public void loadRecipes() {

		// Add base recipe for pipe waterproof.
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.dye, 1, 2));

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes) {
			if (pipe.isShapeless) {
				CoreProxy.proxy.addShapelessRecipe(pipe.result, pipe.input);
			} else {
				CoreProxy.proxy.addCraftingRecipe(pipe.result, pipe.input);
			}
		}

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(filteredBufferBlock, 1),
				new Object[]{"wdw", "wcw", "wpw", 'w', "plankWood", 'd',
			BuildCraftTransport.pipeItemsDiamond, 'c', Blocks.chest, 'p',
			Blocks.piston});

		//Facade turning helper
		GameRegistry.addRecipe(facadeItem.new FacadeRecipe());
		RecipeSorter.register("facadeTurningHelper", ItemFacade.FacadeRecipe.class, RecipeSorter.Category.SHAPELESS, "");

		BuildcraftRecipes.assemblyTable.addRecipe(1000, new ItemStack(plugItem, 8), new ItemStack(pipeStructureCobblestone));
	}

	@EventHandler
	public void processIMCRequests(IMCEvent event) {
		InterModComms.processIMC(event);
	}

	public static Item buildPipe(int defaultID, Class<? extends Pipe> clas, String descr, Object... ingredients) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0)) + clas.getSimpleName().substring(1);

		ItemPipe res = BlockGenericPipe.registerPipe(clas);
		res.setUnlocalizedName(clas.getSimpleName());
		LanguageRegistry.addName(res, descr);

		// Add appropriate recipe to temporary list
		PipeRecipe recipe = new PipeRecipe();

		if (ingredients.length == 3) {
			recipe.result = new ItemStack(res, 8);
			recipe.input = new Object[]{"ABC", 'A', ingredients[0], 'B', ingredients[1], 'C', ingredients[2]};

			pipeRecipes.add(recipe);
		} else if (ingredients.length == 2) {
			recipe.isShapeless = true;
			recipe.result = new ItemStack(res, 1);
			recipe.input = new Object[]{ingredients[0], ingredients[1]};

			pipeRecipes.add(recipe);

			if (ingredients[1] instanceof ItemPipe) {
				PipeRecipe uncraft = new PipeRecipe();
				uncraft.isShapeless = true;
				uncraft.input = new Object[]{new ItemStack(res)};
				uncraft.result = new ItemStack((Item) ingredients[1]);
				pipeRecipes.add(uncraft);
			}
		}

		return res;
	}

}
