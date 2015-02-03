/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.RecipeSorter;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.JavaTools;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.compat.CompatHooks;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.PowerMode;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.ColorUtils;
import buildcraft.robots.ItemRobotStation;
import buildcraft.robots.RobotStationPluggable;
import buildcraft.silicon.ItemRedstoneChipset.Chipset;
import buildcraft.transport.BlockFilteredBuffer;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.FacadePluggable;
import buildcraft.transport.GuiHandler;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemGateCopier;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.ItemPipeWire;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeActionProvider;
import buildcraft.transport.PipeColoringRecipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTriggerProvider;
import buildcraft.transport.TileFilteredBuffer;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.WireIconProvider;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.gates.GatePluggable;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.network.PacketHandlerTransport;
import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeFluidsDiamond;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeFluidsGold;
import buildcraft.transport.pipes.PipeFluidsIron;
import buildcraft.transport.pipes.PipeFluidsQuartz;
import buildcraft.transport.pipes.PipeFluidsSandstone;
import buildcraft.transport.pipes.PipeFluidsStone;
import buildcraft.transport.pipes.PipeFluidsVoid;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsClay;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsDaizuli;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsEmzuli;
import buildcraft.transport.pipes.PipeItemsGold;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsLapis;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsQuartz;
import buildcraft.transport.pipes.PipeItemsSandstone;
import buildcraft.transport.pipes.PipeItemsStone;
import buildcraft.transport.pipes.PipeItemsStripes;
import buildcraft.transport.pipes.PipeItemsVoid;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerEmerald;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerIron;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerSandstone;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerVoid;
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.pipes.PipeStructureCobblestone;
import buildcraft.transport.pluggable.ItemLens;
import buildcraft.transport.pluggable.ItemPlug;
import buildcraft.transport.pluggable.LensPluggable;
import buildcraft.transport.pluggable.PlugPluggable;
import buildcraft.transport.recipes.AdvancedFacadeRecipe;
import buildcraft.transport.recipes.GateExpansionRecipe;
import buildcraft.transport.recipes.GateLogicSwapRecipe;
import buildcraft.transport.schematics.BptItemPipeFilters;
import buildcraft.transport.schematics.BptPipeIron;
import buildcraft.transport.schematics.BptPipeWooden;
import buildcraft.transport.schematics.SchematicPipe;
import buildcraft.transport.statements.ActionEnergyPulsar;
import buildcraft.transport.statements.ActionExtractionPreset;
import buildcraft.transport.statements.ActionParameterSignal;
import buildcraft.transport.statements.ActionPipeColor;
import buildcraft.transport.statements.ActionPipeDirection;
import buildcraft.transport.statements.ActionPowerLimiter;
import buildcraft.transport.statements.ActionRedstoneFaderOutput;
import buildcraft.transport.statements.ActionSignalOutput;
import buildcraft.transport.statements.ActionSingleEnergyPulse;
import buildcraft.transport.statements.ActionValve;
import buildcraft.transport.statements.ActionValve.ValveState;
import buildcraft.transport.statements.TriggerClockTimer;
import buildcraft.transport.statements.TriggerClockTimer.Time;
import buildcraft.transport.statements.TriggerParameterSignal;
import buildcraft.transport.statements.TriggerPipeContents;
import buildcraft.transport.statements.TriggerPipeContents.PipeContents;
import buildcraft.transport.statements.TriggerPipeSignal;
import buildcraft.transport.statements.TriggerRedstoneFaderInput;
import buildcraft.transport.stripes.StripesHandlerArrow;
import buildcraft.transport.stripes.StripesHandlerBucket;
import buildcraft.transport.stripes.StripesHandlerRightClick;
import buildcraft.transport.stripes.StripesHandlerShears;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(version = Version.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTransport extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Transport")
	public static BuildCraftTransport instance;

	public static float pipeDurability;
    public static int pipeFluidsBaseFlowRate;
    public static boolean facadeTreatBlacklistAsWhitelist;
    public static boolean additionalWaterproofingRecipe;

	public static BlockGenericPipe genericPipeBlock;
	public static BlockFilteredBuffer filteredBufferBlock;

	public static Item pipeWaterproof;
	public static Item pipeGate;
	public static Item pipeWire;
	public static Item plugItem;
	public static Item lensItem;
	public static Item powerAdapterItem;
	public static Item robotStationItem;
	public static Item pipeStructureCobblestone;
	public static Item gateCopier;
	public static ItemFacade facadeItem;

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
	public static Item pipeItemsStripes;
    public static Item pipeItemsClay;
	public static Item pipeFluidsWood;
	public static Item pipeFluidsCobblestone;
	public static Item pipeFluidsStone;
	public static Item pipeFluidsQuartz;
	public static Item pipeFluidsIron;
	public static Item pipeFluidsGold;
	public static Item pipeFluidsVoid;
	public static Item pipeFluidsSandstone;
	public static Item pipeFluidsEmerald;
	public static Item pipeFluidsDiamond;
	public static Item pipePowerWood;
	public static Item pipePowerCobblestone;
	public static Item pipePowerStone;
	public static Item pipePowerQuartz;
	public static Item pipePowerIron;
	public static Item pipePowerGold;
	public static Item pipePowerDiamond;
	public static Item pipePowerEmerald;
    public static Item pipePowerSandstone;
    public static Item pipePowerVoid;

	public static int groupItemsTrigger;
	public static String[] facadeBlacklist;

	public static ITriggerInternal[] triggerPipe = new ITriggerInternal[PipeContents.values().length];
	public static ITriggerInternal[] triggerPipeWireActive = new ITriggerInternal[PipeWire.values().length];
	public static ITriggerInternal[] triggerPipeWireInactive = new ITriggerInternal[PipeWire.values().length];
	public static ITriggerInternal[] triggerTimer = new ITriggerInternal[TriggerClockTimer.Time.VALUES.length];
	public static ITriggerInternal[] triggerRedstoneLevel = new ITriggerInternal[15];
	public static IActionInternal[] actionPipeWire = new ActionSignalOutput[PipeWire.values().length];
	public static IActionInternal actionEnergyPulser = new ActionEnergyPulsar();
	public static IActionInternal actionSingleEnergyPulse = new ActionSingleEnergyPulse();
	public static IActionInternal[] actionPipeColor = new IActionInternal[16];
	public static IActionInternal[] actionPipeDirection = new IActionInternal[16];
	public static IActionInternal[] actionPowerLimiter = new IActionInternal[7];
	public static IActionInternal[] actionRedstoneLevel = new IActionInternal[15];
	public static IActionInternal actionExtractionPresetRed = new ActionExtractionPreset(EnumColor.RED);
	public static IActionInternal actionExtractionPresetBlue = new ActionExtractionPreset(EnumColor.BLUE);
	public static IActionInternal actionExtractionPresetGreen = new ActionExtractionPreset(EnumColor.GREEN);
	public static IActionInternal actionExtractionPresetYellow = new ActionExtractionPreset(EnumColor.YELLOW);
    public static IActionInternal[] actionValve = new IActionInternal[4];

    public static boolean debugPrintFacadeList = false;

	public static float gateCostMultiplier = 1.0F;

	private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();

	public IIconProvider pipeIconProvider = new PipeIconProvider();
	public IIconProvider wireIconProvider = new WireIconProvider();

	private static class PipeRecipe {
		boolean isShapeless = false; // pipe recipes come shaped and unshaped.
		ItemStack result;
		Object[] input;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		try {
			Property durability = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.durability", DefaultProps.PIPES_DURABILITY);
			durability.comment = "How long a pipe will take to break";
			pipeDurability = (float) durability.getDouble(DefaultProps.PIPES_DURABILITY);

			Property baseFlowRate = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.fluids.baseFlowRate", DefaultProps.PIPES_FLUIDS_BASE_FLOW_RATE);
			pipeFluidsBaseFlowRate = baseFlowRate.getInt();

			Property printFacadeList = BuildCraftCore.mainConfiguration.get("debug", "facades.printFacadeList", false);
			debugPrintFacadeList = printFacadeList.getBoolean();

			Property enableAdditionalWaterproofingRecipe = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.fluids.enableAdditionalWaterproofingRecipe", true);
			enableAdditionalWaterproofingRecipe.comment = "Enable the slimeball based pipe waterproofing recipe";
			additionalWaterproofingRecipe = enableAdditionalWaterproofingRecipe.getBoolean();

			gateCostMultiplier = BuildCraftCore.mainConfiguration.getFloat("gate.recipeCostMultiplier", Configuration.CATEGORY_GENERAL, 1.0F, 0.001F, 1000.0F, "The multiplier for gate recipe cost.");

			filteredBufferBlock = new BlockFilteredBuffer();
			CoreProxy.proxy.registerBlock(filteredBufferBlock.setBlockName("filteredBufferBlock"));

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
					Block.blockRegistry.getNameForObject(Blocks.leaves),
					Block.blockRegistry.getNameForObject(Blocks.leaves2),
					Block.blockRegistry.getNameForObject(Blocks.lit_pumpkin),
					Block.blockRegistry.getNameForObject(Blocks.lit_redstone_lamp),
					Block.blockRegistry.getNameForObject(Blocks.mob_spawner),
					Block.blockRegistry.getNameForObject(Blocks.monster_egg),
					Block.blockRegistry.getNameForObject(Blocks.redstone_lamp),
					Block.blockRegistry.getNameForObject(Blocks.double_stone_slab),
					Block.blockRegistry.getNameForObject(Blocks.double_wooden_slab),
					Block.blockRegistry.getNameForObject(Blocks.sponge),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.architectBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.builderBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.fillerBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftBuilders.libraryBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.autoWorkbenchBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.floodGateBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.miningWellBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.pumpBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftFactory.quarryBlock)),
					JavaTools.surroundWithQuotes(Block.blockRegistry.getNameForObject(BuildCraftTransport.filteredBufferBlock)),
			});

			facadeBlacklistProp.comment = "Blocks listed here will not have facades created. The format is modid:blockname.\nFor mods with a | character, the value needs to be surrounded with quotes.";
			facadeBlacklist = facadeBlacklistProp.getStringList();

            Property facadeAsWhitelist = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "facade.treatBlacklistAsWhitelist", false);
            facadeTreatBlacklistAsWhitelist = facadeAsWhitelist.getBoolean();

			pipeWaterproof = new ItemBuildCraft();

			pipeWaterproof.setUnlocalizedName("pipeWaterproof");
			CoreProxy.proxy.registerItem(pipeWaterproof);

			genericPipeBlock = (BlockGenericPipe) CompatHooks.INSTANCE.getBlock(BlockGenericPipe.class);

			CoreProxy.proxy.registerBlock(genericPipeBlock.setBlockName("pipeBlock"), ItemBlock.class);

			pipeItemsWood = buildPipe(PipeItemsWood.class, "Wooden Transport Pipe", CreativeTabBuildCraft.PIPES, "plankWood", Blocks.glass, "plankWood");
			pipeItemsEmerald = buildPipe(PipeItemsEmerald.class, "Emerald Transport Pipe", CreativeTabBuildCraft.PIPES, "gemEmerald", Blocks.glass, "gemEmerald");
			pipeItemsCobblestone = buildPipe(PipeItemsCobblestone.class, "Cobblestone Transport Pipe", CreativeTabBuildCraft.PIPES, "cobblestone", Blocks.glass, "cobblestone");
			pipeItemsStone = buildPipe(PipeItemsStone.class, "Stone Transport Pipe", CreativeTabBuildCraft.PIPES, "stone", Blocks.glass, "stone");
			pipeItemsQuartz = buildPipe(PipeItemsQuartz.class, "Quartz Transport Pipe", CreativeTabBuildCraft.PIPES, "blockQuartz", Blocks.glass, "blockQuartz");
			pipeItemsIron = buildPipe(PipeItemsIron.class, "Iron Transport Pipe", CreativeTabBuildCraft.PIPES, "ingotIron", Blocks.glass, "ingotIron");
			pipeItemsGold = buildPipe(PipeItemsGold.class, "Golden Transport Pipe", CreativeTabBuildCraft.PIPES, "ingotGold", Blocks.glass, "ingotGold");
			pipeItemsDiamond = buildPipe(PipeItemsDiamond.class, "Diamond Transport Pipe", CreativeTabBuildCraft.PIPES, "gemDiamond", Blocks.glass, "gemDiamond");
			pipeItemsObsidian = buildPipe(PipeItemsObsidian.class, "Obsidian Transport Pipe", CreativeTabBuildCraft.PIPES, Blocks.obsidian, Blocks.glass, Blocks.obsidian);
			pipeItemsLapis = buildPipe(PipeItemsLapis.class, "Lapis Transport Pipe", CreativeTabBuildCraft.PIPES, "blockLapis", Blocks.glass, "blockLapis");
			pipeItemsDaizuli = buildPipe(PipeItemsDaizuli.class, "Daizuli Transport Pipe", CreativeTabBuildCraft.PIPES, "blockLapis", Blocks.glass, "gemDiamond");
			pipeItemsSandstone = buildPipe(PipeItemsSandstone.class, "Sandstone Transport Pipe", CreativeTabBuildCraft.PIPES, Blocks.sandstone, Blocks.glass, Blocks.sandstone);
			pipeItemsVoid = buildPipe(PipeItemsVoid.class, "Void Transport Pipe", CreativeTabBuildCraft.PIPES, "dyeBlack", Blocks.glass, "dustRedstone");
			pipeItemsEmzuli = buildPipe(PipeItemsEmzuli.class, "Emzuli Transport Pipe", CreativeTabBuildCraft.PIPES, "blockLapis", Blocks.glass, "gemEmerald");
			pipeItemsStripes = buildPipe(PipeItemsStripes.class, "Stripes Transport Pipe", CreativeTabBuildCraft.PIPES, "gearGold", Blocks.glass, "gearGold");
            pipeItemsClay = buildPipe(PipeItemsClay.class, "Clay Transport Pipe", CreativeTabBuildCraft.PIPES, Blocks.clay, Blocks.glass, Blocks.clay);

			pipeFluidsWood = buildPipe(PipeFluidsWood.class, "Wooden Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsWood);
			pipeFluidsCobblestone = buildPipe(PipeFluidsCobblestone.class, "Cobblestone Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsCobblestone);
			pipeFluidsStone = buildPipe(PipeFluidsStone.class, "Stone Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsStone);
			pipeFluidsQuartz = buildPipe(PipeFluidsQuartz.class, "Quartz Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsQuartz);
			pipeFluidsIron = buildPipe(PipeFluidsIron.class, "Iron Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsIron);
			pipeFluidsGold = buildPipe(PipeFluidsGold.class, "Golden Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsGold);
			pipeFluidsEmerald = buildPipe(PipeFluidsEmerald.class, "Emerald Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsEmerald);
			pipeFluidsDiamond = buildPipe(PipeFluidsDiamond.class, "Diamond Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsDiamond);
			pipeFluidsSandstone = buildPipe(PipeFluidsSandstone.class, "Sandstone Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsSandstone);
			pipeFluidsVoid = buildPipe(PipeFluidsVoid.class, "Void Waterproof Pipe", CreativeTabBuildCraft.PIPES, pipeWaterproof, pipeItemsVoid);

			pipePowerWood = buildPipe(PipePowerWood.class, "Wooden Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsWood);
			pipePowerCobblestone = buildPipe(PipePowerCobblestone.class, "Cobblestone Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsCobblestone);
			pipePowerStone = buildPipe(PipePowerStone.class, "Stone Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsStone);
			pipePowerQuartz = buildPipe(PipePowerQuartz.class, "Quartz Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsQuartz);
			pipePowerIron = buildPipe(PipePowerIron.class, "Iron Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsIron);
			pipePowerGold = buildPipe(PipePowerGold.class, "Golden Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsGold);
			pipePowerDiamond = buildPipe(PipePowerDiamond.class, "Diamond Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsDiamond);
			pipePowerEmerald = buildPipe(PipePowerEmerald.class, "Emerald Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsEmerald);
            pipePowerSandstone = buildPipe(PipePowerSandstone.class, "Sandstone Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsSandstone);
            pipePowerVoid = buildPipe(PipePowerVoid.class, "Void Kinesis Pipe", CreativeTabBuildCraft.PIPES, "dustRedstone", pipeItemsVoid);

            pipeStructureCobblestone = buildPipe(PipeStructureCobblestone.class, "Cobblestone Structure Pipe", CreativeTabBuildCraft.PIPES, Blocks.gravel, pipeItemsCobblestone);

			pipeWire = new ItemPipeWire();
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

			lensItem = new ItemLens();
			lensItem.setUnlocalizedName("pipeLens");
			CoreProxy.proxy.registerItem(lensItem);

			//powerAdapterItem = new ItemPowerAdapter();
			//powerAdapterItem.setUnlocalizedName("pipePowerAdapter");
			//CoreProxy.proxy.registerItem(powerAdapterItem);

			robotStationItem = new ItemRobotStation();
			robotStationItem.setUnlocalizedName("robotStation");
			CoreProxy.proxy.registerItem(robotStationItem);

			gateCopier = new ItemGateCopier();
			CoreProxy.proxy.registerItem(gateCopier);

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

			for (ValveState state : ValveState.VALUES) {
			    actionValve[state.ordinal()] = new ActionValve(state);
			}

			for (PowerMode limit : PowerMode.VALUES) {
				actionPowerLimiter[limit.ordinal()] = new ActionPowerLimiter(limit);
			}
		} finally {
			BuildCraftCore.mainConfiguration.save();
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-TRANSPORT", new BuildCraftChannelHandler(), new PacketHandlerTransport());

		TransportProxy.proxy.registerTileEntities();

		BuilderAPI.schematicRegistry.registerSchematicBlock(genericPipeBlock, SchematicPipe.class);

		new BptPipeIron(pipeItemsIron);
		new BptPipeIron(pipeFluidsIron);
		new BptPipeIron(pipePowerIron);

		new BptPipeWooden(pipeItemsWood);
		new BptPipeWooden(pipeFluidsWood);
		new BptPipeWooden(pipePowerWood);
		new BptPipeWooden(pipeItemsEmerald);

		new BptItemPipeFilters(pipeItemsDiamond);

		StatementManager.registerParameterClass(TriggerParameterSignal.class);
		StatementManager.registerParameterClass(ActionParameterSignal.class);
		StatementManager.registerTriggerProvider(new PipeTriggerProvider());
		StatementManager.registerActionProvider(new PipeActionProvider());

		PipeManager.registerStripesHandler(new StripesHandlerRightClick());
		PipeManager.registerStripesHandler(new StripesHandlerBucket());
		PipeManager.registerStripesHandler(new StripesHandlerArrow());
		PipeManager.registerStripesHandler(new StripesHandlerShears());

		PipeManager.registerPipePluggable(FacadePluggable.class, "facade");
		PipeManager.registerPipePluggable(GatePluggable.class, "gate");
		PipeManager.registerPipePluggable(LensPluggable.class, "lens");
		PipeManager.registerPipePluggable(PlugPluggable.class, "plug");
		PipeManager.registerPipePluggable(RobotStationPluggable.class, "robotStation");

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		TransportProxy.proxy.registerRenderers();
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		facadeItem.initialize();

		if (debugPrintFacadeList) {
			try {
				PrintWriter writer = new PrintWriter("FacadeDebug.txt", "UTF-8");
				writer.println("*** REGISTERED FACADES ***");
				for (ItemStack stack : ItemFacade.allFacades) {
					if (facadeItem.getBlocksForFacade(stack).length > 0) {
						writer.println(Block.blockRegistry.getNameForObject(facadeItem.getBlocksForFacade(stack)[0]) + ":" + facadeItem.getMetaValuesForFacade(stack)[0]);
					}
				}
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void loadRecipes() {
		// Add base recipe for pipe waterproof.
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.dye, 1, 2));
		if(additionalWaterproofingRecipe)
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.slime_ball, 1));

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes) {
			if (pipe.isShapeless) {
				CoreProxy.proxy.addShapelessRecipe(pipe.result, pipe.input);
			} else {
				CoreProxy.proxy.addCraftingRecipe(pipe.result, pipe.input);
			}
		}

		GameRegistry.addRecipe(new PipeColoringRecipe());

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(filteredBufferBlock, 1),
				"wdw", "wcw", "wpw", 'w', "plankWood", 'd',
			BuildCraftTransport.pipeItemsDiamond, 'c', Blocks.chest, 'p',
			Blocks.piston);

		//Facade turning helper
		GameRegistry.addRecipe(facadeItem.new FacadeRecipe());
		RecipeSorter.register("facadeTurningHelper", ItemFacade.FacadeRecipe.class, RecipeSorter.Category.SHAPELESS, "");

		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:pipePlug", 10000, new ItemStack(plugItem, 8),
				new ItemStack(pipeStructureCobblestone));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(robotStationItem), "   ", " I ", "ICI",
				'I', "ingotIron",
				'C', Chipset.GOLD.getStack());

		if (Loader.isModLoaded("BuildCraft|Silicon")) {
			GameRegistry.addShapelessRecipe(new ItemStack(gateCopier, 1), new ItemStack(BuildCraftCore.wrenchItem), Chipset.RED.getStack(1));

			// PIPE WIRE
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redWire", 5000, PipeWire.RED.getStack(8),
					"dyeRed", "dustRedstone", "ingotIron");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:blueWire", 5000, PipeWire.BLUE.getStack(8),
					"dyeBlue", "dustRedstone", "ingotIron");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:greenWire", 5000, PipeWire.GREEN.getStack(8),
					"dyeGreen", "dustRedstone", "ingotIron");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:yellowWire", 5000, PipeWire.YELLOW.getStack(8),
					"dyeYellow", "dustRedstone", "ingotIron");

			// Lens
			for (int i = 0; i < 16; i++) {
				BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:lens:" + i, 10000, new ItemStack(lensItem, 2, i),
						ColorUtils.getOreDictionaryName(15 - i), "blockGlass", "ingotIron");
				BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:filter:" + i, 10000, new ItemStack(lensItem, 2, i + 16),
						ColorUtils.getOreDictionaryName(15 - i), "blockGlass", Blocks.iron_bars);
			}

			// GATES
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:simpleGate", (int) Math.round(100000 * gateCostMultiplier),
					ItemGate.makeGateItem(GateMaterial.REDSTONE, GateLogic.AND), Chipset.RED.getStack(),
					PipeWire.RED.getStack());

			addGateRecipe("Iron", (int) Math.round(200000 * gateCostMultiplier), GateMaterial.IRON, Chipset.IRON, PipeWire.RED, PipeWire.BLUE);
			addGateRecipe("Gold", (int) Math.round(400000 * gateCostMultiplier), GateMaterial.GOLD, Chipset.GOLD, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN);
			addGateRecipe("Quartz", (int) Math.round(600000 * gateCostMultiplier), GateMaterial.QUARTZ, Chipset.QUARTZ, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN);
			addGateRecipe("Diamond", (int) Math.round(800000 * gateCostMultiplier), GateMaterial.DIAMOND, Chipset.DIAMOND, PipeWire.RED, PipeWire.BLUE,
					PipeWire.GREEN, PipeWire.YELLOW);
			addGateRecipe("Emerald", (int) Math.round(1200000 * gateCostMultiplier), GateMaterial.EMERALD, Chipset.EMERALD, PipeWire.RED, PipeWire.BLUE,
					PipeWire.GREEN, PipeWire.YELLOW);

			// REVERSAL RECIPE
			BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateLogicSwapRecipe("buildcraft:gateSwap"));

			// EXPANSIONS
			BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe("buildcraft:expansionPulsar",
					GateExpansionPulsar.INSTANCE, Chipset.PULSATING.getStack()));
			BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe("buildcraft:expansionQuartz",
					GateExpansionTimer.INSTANCE, Chipset.QUARTZ.getStack()));
			BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe("buildcraft:expansionComp",
					GateExpansionRedstoneFader.INSTANCE, Chipset.COMP.getStack()));

			// FACADE
			BuildcraftRecipeRegistry.integrationTable.addRecipe(new AdvancedFacadeRecipe("buildcraft:advancedFacade"));
		}
	}

	private static void addGateRecipe(String materialName, int energyCost, GateMaterial material, Chipset chipset,
			PipeWire... pipeWire) {
		List<ItemStack> temp = new ArrayList<ItemStack>();
		temp.add(chipset.getStack());
		for (PipeWire wire : pipeWire) {
			temp.add(wire.getStack());
		}
		Object[] inputs = temp.toArray();
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:andGate" + materialName, energyCost,
				ItemGate.makeGateItem(material, GateLogic.AND), inputs);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:orGate" + materialName, energyCost,
				ItemGate.makeGateItem(material, GateLogic.OR), inputs);
	}

	@Mod.EventHandler
	public void processIMCRequests(IMCEvent event) {
		InterModComms.processIMC(event);
	}

	public static Item buildPipe(Class<? extends Pipe> clas,
			String descr, CreativeTabBuildCraft creativeTab,
			Object... ingredients) {
		ItemPipe res = BlockGenericPipe.registerPipe(clas, creativeTab);
		res.setUnlocalizedName(clas.getSimpleName());

		// Add appropriate recipes to temporary list
		if (ingredients.length == 3) {
			for (int i = 0; i < 17; i++) {
				PipeRecipe recipe = new PipeRecipe();
				ItemStack glass;

				if (i == 0) {
					glass = new ItemStack(Blocks.glass);
				} else {
					glass = new ItemStack(Blocks.stained_glass, 1, i - 1);
				}

				recipe.result = new ItemStack(res, 8, i);
				recipe.input = new Object[]{"ABC", 'A', ingredients[0], 'B', glass, 'C', ingredients[2]};

				pipeRecipes.add(recipe);
			}
		} else if (ingredients.length == 2) {
			for (int i = 0; i < 17; i++) {
				PipeRecipe recipe = new PipeRecipe();

				Object left = ingredients[0];
				Object right = ingredients[1];

				if (ingredients[1] instanceof ItemPipe) {
					right = new ItemStack((Item) right, 1, i);
				}

				recipe.isShapeless = true;
				recipe.result = new ItemStack(res, 1, i);
				recipe.input = new Object[]{left, right};

				pipeRecipes.add(recipe);

				if (ingredients[1] instanceof ItemPipe) {
					PipeRecipe uncraft = new PipeRecipe();
					uncraft.isShapeless = true;
					uncraft.input = new Object[]{recipe.result};
					uncraft.result = (ItemStack) right;
					pipeRecipes.add(uncraft);
				}
			}
		}

		return res;
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileGenericPipe.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileFilteredBuffer.class.getCanonicalName());
	}
}
