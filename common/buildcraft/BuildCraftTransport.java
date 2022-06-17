/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.PrintWriter;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.lists.ListRegistry;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.BCRegistry;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.PowerMode;
import buildcraft.core.Version;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.transport.BlockFilteredBuffer;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.FacadePluggable;
import buildcraft.transport.IDiamondPipe;
import buildcraft.transport.IMCHandlerTransport;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemGateCopier;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.ItemPipeWire;
import buildcraft.transport.LensFilterHandler;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeActionProvider;
import buildcraft.transport.PipeColoringRecipe;
import buildcraft.transport.PipeEventBus;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTriggerProvider;
import buildcraft.transport.TileFilteredBuffer;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportGuiHandler;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TransportSiliconRecipes;
import buildcraft.transport.WireIconProvider;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GateExpansionLightSensor;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.gates.GatePluggable;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.network.PacketFluidUpdate;
import buildcraft.transport.network.PacketHandlerTransport;
import buildcraft.transport.network.PacketPipeTransportItemStack;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.PipeFluidsClay;
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
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.pipes.PipeStructureCobblestone;
import buildcraft.transport.pluggable.ItemLens;
import buildcraft.transport.pluggable.ItemPlug;
import buildcraft.transport.pluggable.ItemPowerAdapter;
import buildcraft.transport.pluggable.LensPluggable;
import buildcraft.transport.pluggable.PlugPluggable;
import buildcraft.transport.pluggable.PowerAdapterPluggable;
import buildcraft.transport.render.PipeRendererTESR;
import buildcraft.transport.schematics.BptPipeFiltered;
import buildcraft.transport.schematics.BptPipeRotatable;
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
import buildcraft.transport.statements.TriggerLightSensor;
import buildcraft.transport.statements.TriggerParameterSignal;
import buildcraft.transport.statements.TriggerPipeContents;
import buildcraft.transport.statements.TriggerPipeContents.PipeContents;
import buildcraft.transport.statements.TriggerPipeSignal;
import buildcraft.transport.statements.TriggerRedstoneFaderInput;
import buildcraft.transport.stripes.PipeExtensionListener;
import buildcraft.transport.stripes.StripesHandlerArrow;
import buildcraft.transport.stripes.StripesHandlerBucket;
import buildcraft.transport.stripes.StripesHandlerDispenser;
import buildcraft.transport.stripes.StripesHandlerEntityInteract;
import buildcraft.transport.stripes.StripesHandlerHoe;
import buildcraft.transport.stripes.StripesHandlerMinecartDestroy;
import buildcraft.transport.stripes.StripesHandlerPipeWires;
import buildcraft.transport.stripes.StripesHandlerPipes;
import buildcraft.transport.stripes.StripesHandlerPlaceBlock;
import buildcraft.transport.stripes.StripesHandlerPlant;
import buildcraft.transport.stripes.StripesHandlerRightClick;
import buildcraft.transport.stripes.StripesHandlerShears;
import buildcraft.transport.stripes.StripesHandlerUse;

@Mod(version = Version.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTransport extends BuildCraftMod {
	@Mod.Instance("BuildCraft|Transport")
	public static BuildCraftTransport instance;

	public static float pipeDurability;
	public static int pipeFluidsBaseFlowRate;
	public static boolean facadeTreatBlacklistAsWhitelist;
	public static boolean additionalWaterproofingRecipe;
	public static boolean facadeForceNonLaserRecipe;
	public static boolean showAllFacadesCreative;

	public static BlockGenericPipe genericPipeBlock;
	public static BlockFilteredBuffer filteredBufferBlock;

	public static Item pipeWaterproof;
	public static Item pipeGate;
	public static Item pipeWire;
	public static Item plugItem;
	public static Item lensItem;
	public static Item powerAdapterItem;
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
	public static Item pipeFluidsClay;
	public static Item pipePowerWood;
	public static Item pipePowerCobblestone;
	public static Item pipePowerStone;
	public static Item pipePowerQuartz;
	public static Item pipePowerIron;
	public static Item pipePowerGold;
	public static Item pipePowerDiamond;
	public static Item pipePowerEmerald;
	public static Item pipePowerSandstone;

	public static String[] facadeBlacklist;

	public static ITriggerInternal triggerLightSensorBright, triggerLightSensorDark;
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
	public static boolean usePipeLoss = false;

	public static float gateCostMultiplier = 1.0F;

	public static PipeExtensionListener pipeExtensionListener;

	private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();
	private static ChannelHandler transportChannelHandler;

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
			BuildCraftCore.mainConfigManager.register("experimental.kinesisPowerLossOnTravel", false, "Should kinesis pipes lose power over distance (think IC2 or BC pre-3.7)?", ConfigManager.RestartRequirement.WORLD);

			BuildCraftCore.mainConfigManager.register("general.pipes.hardness", DefaultProps.PIPES_DURABILITY, "How hard to break should a pipe be?", ConfigManager.RestartRequirement.NONE);
			BuildCraftCore.mainConfigManager.register("general.pipes.baseFluidRate", DefaultProps.PIPES_FLUIDS_BASE_FLOW_RATE, "What should the base flow rate of a fluid pipe be?", ConfigManager.RestartRequirement.GAME)
					.setMinValue(1).setMaxValue(40);
			BuildCraftCore.mainConfigManager.register("debug.printFacadeList", false, "Print a list of all registered facades.", ConfigManager.RestartRequirement.GAME);
			BuildCraftCore.mainConfigManager.register("general.pipes.facadeShowAllInCreative", true, "Should all BC facades be shown in Creative/NEI, or just a few carefully chosen ones?", ConfigManager.RestartRequirement.GAME);
			BuildCraftCore.mainConfigManager.register("general.pipes.slimeballWaterproofRecipe", false, "Should I enable an alternate Waterproof recipe, based on slimeballs?", ConfigManager.RestartRequirement.GAME);
			BuildCraftCore.mainConfigManager.register("power.gateCostMultiplier", 1.0D, "What should be the multiplier of all gate power costs?", ConfigManager.RestartRequirement.GAME);
			BuildCraftCore.mainConfigManager.register("general.pipes.facadeBlacklist", new String[]{
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
					Block.blockRegistry.getNameForObject(Blocks.sponge)
			}, "What block types should be blacklisted from being a facade?", ConfigManager.RestartRequirement.GAME);
			BuildCraftCore.mainConfigManager.register("general.pipes.facadeBlacklistAsWhitelist", false, "Should the blacklist be treated as a whitelist instead?", ConfigManager.RestartRequirement.GAME);
			BuildCraftCore.mainConfigManager.register("general.pipes.facadeNoLaserRecipe", false, "Should non-laser (crafting table) facade recipes be forced?", ConfigManager.RestartRequirement.GAME);

			reloadConfig(ConfigManager.RestartRequirement.GAME);

			if (showAllFacadesCreative) {
				new BCCreativeTab("facades");
			}

			filteredBufferBlock = new BlockFilteredBuffer();
			BCRegistry.INSTANCE.registerBlock(filteredBufferBlock.setBlockName("filteredBufferBlock"), false);

			pipeWaterproof = new ItemBuildCraft();
			pipeWaterproof.setUnlocalizedName("pipeWaterproof");
			BCRegistry.INSTANCE.registerItem(pipeWaterproof, false);

			genericPipeBlock = (BlockGenericPipe) CompatHooks.INSTANCE.getBlock(BlockGenericPipe.class);
			BCRegistry.INSTANCE.registerBlock(genericPipeBlock.setBlockName("pipeBlock"), ItemBlock.class, true);

			pipeItemsWood = buildPipe(PipeItemsWood.class, "plankWood", "blockGlassColorless", "plankWood");
			pipeItemsEmerald = buildPipe(PipeItemsEmerald.class, "gemEmerald", "blockGlassColorless", "gemEmerald");
			pipeItemsCobblestone = buildPipe(PipeItemsCobblestone.class, "cobblestone", "blockGlassColorless", "cobblestone");
			pipeItemsStone = buildPipe(PipeItemsStone.class, "stone", "blockGlassColorless", "stone");
			pipeItemsQuartz = buildPipe(PipeItemsQuartz.class, "blockQuartz", "blockGlassColorless", "blockQuartz");
			pipeItemsIron = buildPipe(PipeItemsIron.class, "ingotIron", "blockGlassColorless", "ingotIron");
			pipeItemsGold = buildPipe(PipeItemsGold.class, "ingotGold", "blockGlassColorless", "ingotGold");
			pipeItemsDiamond = buildPipe(PipeItemsDiamond.class, "gemDiamond", "blockGlassColorless", "gemDiamond");
			pipeItemsObsidian = buildPipe(PipeItemsObsidian.class, Blocks.obsidian, "blockGlassColorless", Blocks.obsidian);
			pipeItemsLapis = buildPipe(PipeItemsLapis.class, "blockLapis", "blockGlassColorless", "blockLapis");
			pipeItemsDaizuli = buildPipe(PipeItemsDaizuli.class, "blockLapis", "blockGlassColorless", "gemDiamond");
			pipeItemsSandstone = buildPipe(PipeItemsSandstone.class, Blocks.sandstone, "blockGlassColorless", Blocks.sandstone);
			pipeItemsVoid = buildPipe(PipeItemsVoid.class, "dyeBlack", "blockGlassColorless", "dustRedstone");
			pipeItemsEmzuli = buildPipe(PipeItemsEmzuli.class, "blockLapis", "blockGlassColorless", "gemEmerald");
			pipeItemsStripes = buildPipe(PipeItemsStripes.class, "gearGold", "blockGlassColorless", "gearGold");
			pipeItemsClay = buildPipe(PipeItemsClay.class, Blocks.clay, "blockGlassColorless", Blocks.clay);

			pipeFluidsWood = buildPipe(PipeFluidsWood.class, pipeWaterproof, pipeItemsWood);
			pipeFluidsCobblestone = buildPipe(PipeFluidsCobblestone.class, pipeWaterproof, pipeItemsCobblestone);
			pipeFluidsStone = buildPipe(PipeFluidsStone.class, pipeWaterproof, pipeItemsStone);
			pipeFluidsQuartz = buildPipe(PipeFluidsQuartz.class, pipeWaterproof, pipeItemsQuartz);
			pipeFluidsIron = buildPipe(PipeFluidsIron.class, pipeWaterproof, pipeItemsIron);
			pipeFluidsGold = buildPipe(PipeFluidsGold.class, pipeWaterproof, pipeItemsGold);
			pipeFluidsEmerald = buildPipe(PipeFluidsEmerald.class, pipeWaterproof, pipeItemsEmerald);
			pipeFluidsDiamond = buildPipe(PipeFluidsDiamond.class, pipeWaterproof, pipeItemsDiamond);
			pipeFluidsSandstone = buildPipe(PipeFluidsSandstone.class, pipeWaterproof, pipeItemsSandstone);
			pipeFluidsVoid = buildPipe(PipeFluidsVoid.class, pipeWaterproof, pipeItemsVoid);
			pipeFluidsClay = buildPipe(PipeFluidsClay.class, pipeWaterproof, pipeItemsClay);

			pipePowerWood = buildPipe(PipePowerWood.class, "dustRedstone", pipeItemsWood);
			pipePowerCobblestone = buildPipe(PipePowerCobblestone.class, "dustRedstone", pipeItemsCobblestone);
			pipePowerStone = buildPipe(PipePowerStone.class, "dustRedstone", pipeItemsStone);
			pipePowerQuartz = buildPipe(PipePowerQuartz.class, "dustRedstone", pipeItemsQuartz);
			pipePowerIron = buildPipe(PipePowerIron.class, "dustRedstone", pipeItemsIron);
			pipePowerGold = buildPipe(PipePowerGold.class, "dustRedstone", pipeItemsGold);
			pipePowerDiamond = buildPipe(PipePowerDiamond.class, "dustRedstone", pipeItemsDiamond);
			pipePowerEmerald = buildPipe(PipePowerEmerald.class, "dustRedstone", pipeItemsEmerald);
			pipePowerSandstone = buildPipe(PipePowerSandstone.class, "dustRedstone", pipeItemsSandstone);

			pipeStructureCobblestone = buildPipe(PipeStructureCobblestone.class, Blocks.cobblestone, Blocks.gravel, Blocks.cobblestone);

			pipeWire = new ItemPipeWire();
			BCRegistry.INSTANCE.registerItem(pipeWire, false);
			PipeWire.item = pipeWire;

			pipeGate = new ItemGate();
			pipeGate.setUnlocalizedName("pipeGate");
			if (Loader.isModLoaded("BuildCraft|Silicon") && BCRegistry.INSTANCE.isItemEnabled(pipeGate)) {
				new BCCreativeTab("gates");
			}
			BCRegistry.INSTANCE.registerItem(pipeGate, false);

			facadeItem = new ItemFacade();
			facadeItem.setUnlocalizedName("pipeFacade");
			BCRegistry.INSTANCE.registerItem(facadeItem, false);
			FacadeAPI.facadeItem = facadeItem;

			plugItem = new ItemPlug();
			plugItem.setUnlocalizedName("pipePlug");
			BCRegistry.INSTANCE.registerItem(plugItem, false);

			lensItem = new ItemLens();
			lensItem.setUnlocalizedName("pipeLens");
			BCRegistry.INSTANCE.registerItem(lensItem, false);

			powerAdapterItem = new ItemPowerAdapter();
			powerAdapterItem.setUnlocalizedName("pipePowerAdapter");
			BCRegistry.INSTANCE.registerItem(powerAdapterItem, false);

			gateCopier = new ItemGateCopier();
			BCRegistry.INSTANCE.registerItem(gateCopier, false);

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

			triggerLightSensorBright = new TriggerLightSensor(true);
			triggerLightSensorDark = new TriggerLightSensor(false);
		} finally {
			BuildCraftCore.mainConfiguration.save();
		}

		InterModComms.registerHandler(new IMCHandlerTransport());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		transportChannelHandler = new ChannelHandler();
		MinecraftForge.EVENT_BUS.register(this);

		transportChannelHandler.registerPacketType(PacketFluidUpdate.class);
		transportChannelHandler.registerPacketType(PacketPipeTransportItemStack.class);
		transportChannelHandler.registerPacketType(PacketPipeTransportItemStackRequest.class);
		transportChannelHandler.registerPacketType(PacketPipeTransportTraveler.class);
		transportChannelHandler.registerPacketType(PacketPowerUpdate.class);

		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-TRANSPORT", transportChannelHandler, new PacketHandlerTransport());

		TransportProxy.proxy.registerTileEntities();

		BuilderAPI.schematicRegistry.registerSchematicBlock(genericPipeBlock, SchematicPipe.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(filteredBufferBlock, SchematicTile.class);

		new BptPipeRotatable(pipeItemsWood);
		new BptPipeRotatable(pipeFluidsWood);
		new BptPipeRotatable(pipeItemsIron);
		new BptPipeRotatable(pipeFluidsIron);
		new BptPipeRotatable(pipeItemsEmerald);
		new BptPipeRotatable(pipeFluidsEmerald);

		new BptPipeRotatable(pipeItemsDaizuli);
		new BptPipeRotatable(pipeItemsEmzuli);

		for (Item itemPipe : BlockGenericPipe.pipes.keySet()) {
			Class<? extends Pipe<?>> klazz = BlockGenericPipe.pipes.get(itemPipe);

			if (IDiamondPipe.class.isAssignableFrom(klazz)) {
				new BptPipeFiltered(itemPipe);
			}
		}

		PipeEventBus.registerGlobalHandler(new LensFilterHandler());

		if (BCCreativeTab.isPresent("pipes")) {
			BCCreativeTab.get("pipes").setIcon(new ItemStack(BuildCraftTransport.pipeItemsDiamond, 1));
		}
		if (BCCreativeTab.isPresent("facades")) {
			BCCreativeTab.get("facades").setIcon(facadeItem.getFacadeForBlock(Blocks.brick_block, 0));
		}
		if (BCCreativeTab.isPresent("gates")) {
			BCCreativeTab.get("gates").setIcon(ItemGate.makeGateItem(GateMaterial.DIAMOND, GateLogic.AND));
		}

		StatementManager.registerParameterClass(TriggerParameterSignal.class);
		StatementManager.registerParameterClass(ActionParameterSignal.class);
		StatementManager.registerTriggerProvider(new PipeTriggerProvider());
		StatementManager.registerActionProvider(new PipeActionProvider());

		// Item use stripes handlers
		PipeManager.registerStripesHandler(new StripesHandlerRightClick(), -32768);
		PipeManager.registerStripesHandler(new StripesHandlerDispenser(), -49152);
		PipeManager.registerStripesHandler(new StripesHandlerPlant(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerBucket(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerArrow(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerShears(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerPipes(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerPipeWires(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerEntityInteract(), 0);
		PipeManager.registerStripesHandler(new StripesHandlerPlaceBlock(), -65536);
		PipeManager.registerStripesHandler(new StripesHandlerUse(), -131072);
		PipeManager.registerStripesHandler(new StripesHandlerHoe(), 0);

		StripesHandlerDispenser.items.add(ItemMinecart.class);
		StripesHandlerRightClick.items.add(Items.egg);
		StripesHandlerRightClick.items.add(Items.snowball);
		StripesHandlerRightClick.items.add(Items.experience_bottle);
		StripesHandlerUse.items.add(Items.fireworks);

		// Block breaking stripes handlers
		PipeManager.registerStripesHandler(new StripesHandlerMinecartDestroy(), 0);

		PipeManager.registerPipePluggable(FacadePluggable.class, "facade");
		PipeManager.registerPipePluggable(GatePluggable.class, "gate");
		PipeManager.registerPipePluggable(LensPluggable.class, "lens");
		PipeManager.registerPipePluggable(PlugPluggable.class, "plug");
		PipeManager.registerPipePluggable(PowerAdapterPluggable.class, "powerAdapter");

		GateExpansions.registerExpansion(GateExpansionPulsar.INSTANCE);
		GateExpansions.registerExpansion(GateExpansionTimer.INSTANCE);
		GateExpansions.registerExpansion(GateExpansionRedstoneFader.INSTANCE);
		GateExpansions.registerExpansion(GateExpansionLightSensor.INSTANCE, new ItemStack(Blocks.daylight_detector));

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		TransportProxy.proxy.registerRenderers();
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new TransportGuiHandler());
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

		ListRegistry.itemClassAsType.add(ItemPipe.class);
		ListRegistry.itemClassAsType.add(ItemGate.class);
		ListRegistry.itemClassAsType.add(ItemFacade.class);
		ListRegistry.itemClassAsType.add(ItemPipeWire.class);
	}

	public void reloadConfig(ConfigManager.RestartRequirement restartType) {
		if (restartType == ConfigManager.RestartRequirement.GAME) {
			facadeTreatBlacklistAsWhitelist = BuildCraftCore.mainConfigManager.get("general.pipes.facadeBlacklistAsWhitelist").getBoolean();
			facadeBlacklist = BuildCraftCore.mainConfigManager.get("general.pipes.facadeBlacklist").getStringList();
			gateCostMultiplier = (float) BuildCraftCore.mainConfigManager.get("power.gateCostMultiplier").getDouble();
			additionalWaterproofingRecipe = BuildCraftCore.mainConfigManager.get("general.pipes.slimeballWaterproofRecipe").getBoolean();
			debugPrintFacadeList = BuildCraftCore.mainConfigManager.get("debug.printFacadeList").getBoolean();
			pipeFluidsBaseFlowRate = BuildCraftCore.mainConfigManager.get("general.pipes.baseFluidRate").getInt();
			facadeForceNonLaserRecipe = BuildCraftCore.mainConfigManager.get("general.pipes.facadeNoLaserRecipe").getBoolean();
			showAllFacadesCreative = BuildCraftCore.mainConfigManager.get("general.pipes.facadeShowAllInCreative").getBoolean();

			reloadConfig(ConfigManager.RestartRequirement.WORLD);
		} else if (restartType == ConfigManager.RestartRequirement.WORLD) {
			usePipeLoss = BuildCraftCore.mainConfigManager.get("experimental.kinesisPowerLossOnTravel").getBoolean();

			reloadConfig(ConfigManager.RestartRequirement.NONE);
		} else {
			pipeDurability = (float) BuildCraftCore.mainConfigManager.get("general.pipes.hardness").getDouble();

			if (BuildCraftCore.mainConfiguration.hasChanged()) {
				BuildCraftCore.mainConfiguration.save();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if ("BuildCraft|Core".equals(event.modID)) {
			reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() == 0) {
			for (Item i : BlockGenericPipe.pipes.keySet()) {
				Pipe<?> dummyPipe = BlockGenericPipe.createPipe(i);
				if (dummyPipe != null) {
					dummyPipe.getIconProvider().registerIcons(event.map);
				}
			}

			wireIconProvider.registerIcons(event.map);

			for (GateDefinition.GateMaterial material : GateDefinition.GateMaterial.VALUES) {
				material.registerBlockIcon(event.map);
			}

			for (GateDefinition.GateLogic logic : GateDefinition.GateLogic.VALUES) {
				logic.registerBlockIcon(event.map);
			}

			for (IGateExpansion expansion : GateExpansions.getExpansions()) {
				expansion.registerBlockOverlay(event.map);
			}

			PipeRendererTESR.INSTANCE.onTextureReload();
		}
	}

	@Mod.EventHandler
	public void serverLoading(FMLServerStartingEvent event) {
		pipeExtensionListener = new PipeExtensionListener();
		FMLCommonHandler.instance().bus().register(pipeExtensionListener);
	}

	@Mod.EventHandler
	public void serverUnloading(FMLServerStoppingEvent event) {
		// One last tick
		for (WorldServer w : DimensionManager.getWorlds()) {
			pipeExtensionListener.tick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, w));
		}
		FMLCommonHandler.instance().bus().unregister(pipeExtensionListener);
		pipeExtensionListener = null;
	}

	public void loadRecipes() {
		// Add base recipe for pipe waterproof.
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.dye, 1, 2));
		if (additionalWaterproofingRecipe) {
			GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.slime_ball));
		}

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes) {
			if (pipe.isShapeless) {
				BCRegistry.INSTANCE.addShapelessRecipe(pipe.result, pipe.input);
			} else {
				BCRegistry.INSTANCE.addCraftingRecipe(pipe.result, pipe.input);
			}
		}

		GameRegistry.addRecipe(new PipeColoringRecipe());
		RecipeSorter.register("buildcraft:pipecoloring", PipeColoringRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(filteredBufferBlock, 1),
				"wdw", "wcw", "wpw", 'w', "plankWood", 'd',
				BuildCraftTransport.pipeItemsDiamond, 'c', "chestWood", 'p',
				Blocks.piston);

		//Facade turning helper
		GameRegistry.addRecipe(facadeItem.new FacadeRecipe());
		RecipeSorter.register("facadeTurningHelper", ItemFacade.FacadeRecipe.class, RecipeSorter.Category.SHAPELESS, "");

		// Pipe Plug
		GameRegistry.addShapelessRecipe(new ItemStack(plugItem, 4), new ItemStack(pipeStructureCobblestone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(powerAdapterItem, 4), "scs", "sbs", "sas", 's', pipeStructureCobblestone, 'a', Items.redstone, 'b', "gearStone", 'c', "ingotGold"));

		if (Loader.isModLoaded("BuildCraft|Silicon")) {
			TransportSiliconRecipes.loadSiliconRecipes();
		} else {
			BCLog.logger.warn("**********************************************");
			BCLog.logger.warn("*   You are using the BuildCraft Transport   *");
			BCLog.logger.warn("*  module WITHOUT the Silicon module. Gates  *");
			BCLog.logger.warn("*           will not be available.           *");
			BCLog.logger.warn("**********************************************");

			// Alternate recipes
			// Lenses, Filters
			for (int i = 0; i < 16; i++) {
				String dye = ColorUtils.getOreDictionaryName(15 - i);
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lensItem, 8, i),
						"OSO", "SGS", "OSO",
						'O', "ingotIron", 'S', dye, 'G', "blockGlass"
				));
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lensItem, 8, i + 16),
						"OSO", "SGS", "OSO",
						'O', Blocks.iron_bars, 'S', dye, 'G', "blockGlass"
				));
			}
		}
	}

	@Mod.EventHandler
	public void processIMCRequests(IMCEvent event) {
		InterModComms.processIMC(event);
	}

	public static Item buildPipe(Class<? extends Pipe<?>> clas, Object... ingredients) {
		return buildPipe(clas, BCCreativeTab.get("pipes"), ingredients);
	}

	@Deprecated
	public static Item buildPipe(Class<? extends Pipe<?>> clas,
								 String descr, BCCreativeTab creativeTab,
								 Object... ingredients) {
		return buildPipe(clas, creativeTab, ingredients);
	}

	public static Item buildPipe(Class<? extends Pipe<?>> clas, BCCreativeTab creativeTab,
								 Object... ingredients) {
		if (!BCRegistry.INSTANCE.isEnabled("pipes", clas.getSimpleName())) {
			return null;
		}

		ItemPipe res = BlockGenericPipe.registerPipe(clas, creativeTab);
		res.setUnlocalizedName(clas.getSimpleName());

		for (Object o : ingredients) {
			if (o == null) {
				return res;
			}
		}

		// Add appropriate recipes to temporary list
		if (ingredients.length == 3) {
			for (int i = 0; i < 17; i++) {
				PipeRecipe recipe = new PipeRecipe();
				Object glass;

				if (i == 0) {
					glass = ingredients[1];
				} else {
					glass = "blockGlass" + EnumColor.fromId(15 - (i - 1)).getName();
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

				if (ingredients[1] instanceof ItemPipe && clas != PipeStructureCobblestone.class) {
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

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
			if (mapping.type == GameRegistry.Type.ITEM) {
				if (mapping.name.equals("BuildCraft|Transport:robotStation")) {
					mapping.remap((Item) Item.itemRegistry.getObject("BuildCraft|Robotics:robotStation"));
				}
			}
		}
	}
}
