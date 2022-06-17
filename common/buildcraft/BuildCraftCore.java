/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.File;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.IIcon;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.crops.CropManager;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.lists.ListRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.tablet.TabletAPI;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.AchievementManager;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.BCRegistry;
import buildcraft.core.BlockBuildTool;
import buildcraft.core.BlockEngine;
import buildcraft.core.BlockMarker;
import buildcraft.core.BlockPathMarker;
import buildcraft.core.BlockSpring;
import buildcraft.core.CompatHooks;
import buildcraft.core.CoreGuiHandler;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.CoreSiliconRecipes;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemDebugger;
import buildcraft.core.ItemGear;
import buildcraft.core.ItemList;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.ItemPaintbrush;
import buildcraft.core.ItemSpring;
import buildcraft.core.ItemWrench;
import buildcraft.core.SchematicEngine;
import buildcraft.core.SpringPopulate;
import buildcraft.core.TickHandlerCore;
import buildcraft.core.TileEngineWood;
import buildcraft.core.TilePathMarker;
import buildcraft.core.Version;
import buildcraft.core.blueprints.BuildingSlotMapIterator;
import buildcraft.core.blueprints.SchematicHelper;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.builders.patterns.FillerRegistry;
import buildcraft.core.builders.patterns.PatternBox;
import buildcraft.core.builders.patterns.PatternClear;
import buildcraft.core.builders.patterns.PatternCylinder;
import buildcraft.core.builders.patterns.PatternFill;
import buildcraft.core.builders.patterns.PatternFlatten;
import buildcraft.core.builders.patterns.PatternFrame;
import buildcraft.core.builders.patterns.PatternHorizon;
import buildcraft.core.builders.patterns.PatternParameterCenter;
import buildcraft.core.builders.patterns.PatternParameterHollow;
import buildcraft.core.builders.patterns.PatternParameterXZDir;
import buildcraft.core.builders.patterns.PatternParameterYDir;
import buildcraft.core.builders.patterns.PatternPyramid;
import buildcraft.core.builders.patterns.PatternStairs;
import buildcraft.core.builders.schematics.SchematicIgnore;
import buildcraft.core.command.SubCommandChangelog;
import buildcraft.core.command.SubCommandDeop;
import buildcraft.core.command.SubCommandOp;
import buildcraft.core.command.SubCommandVersion;
import buildcraft.core.config.BuildCraftConfiguration;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.crops.CropHandlerPlantable;
import buildcraft.core.crops.CropHandlerReeds;
import buildcraft.core.lib.commands.RootCommand;
import buildcraft.core.lib.engines.ItemEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.render.FluidRenderer;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.XorShift128Random;
import buildcraft.core.list.ListMatchHandlerArmor;
import buildcraft.core.list.ListMatchHandlerClass;
import buildcraft.core.list.ListMatchHandlerFluid;
import buildcraft.core.list.ListMatchHandlerOreDictionary;
import buildcraft.core.list.ListMatchHandlerTools;
import buildcraft.core.list.ListOreDictionaryCache;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.network.PacketHandlerCore;
import buildcraft.core.properties.WorldPropertyIsDirt;
import buildcraft.core.properties.WorldPropertyIsFarmland;
import buildcraft.core.properties.WorldPropertyIsFluidSource;
import buildcraft.core.properties.WorldPropertyIsHarvestable;
import buildcraft.core.properties.WorldPropertyIsLeaf;
import buildcraft.core.properties.WorldPropertyIsOre;
import buildcraft.core.properties.WorldPropertyIsReplaceable;
import buildcraft.core.properties.WorldPropertyIsShoveled;
import buildcraft.core.properties.WorldPropertyIsSoft;
import buildcraft.core.properties.WorldPropertyIsWood;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.IntegrationRecipeManager;
import buildcraft.core.recipes.ProgrammingRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.render.BlockHighlightHandler;
import buildcraft.core.render.RenderLEDTile;
import buildcraft.core.render.RenderLaser;
import buildcraft.core.statements.ActionMachineControl;
import buildcraft.core.statements.ActionRedstoneOutput;
import buildcraft.core.statements.DefaultActionProvider;
import buildcraft.core.statements.DefaultTriggerProvider;
import buildcraft.core.statements.StatementParameterDirection;
import buildcraft.core.statements.StatementParameterItemStackExact;
import buildcraft.core.statements.StatementParameterRedstoneGateSideOnly;
import buildcraft.core.statements.TriggerEnergy;
import buildcraft.core.statements.TriggerFluidContainer;
import buildcraft.core.statements.TriggerFluidContainerLevel;
import buildcraft.core.statements.TriggerInventory;
import buildcraft.core.statements.TriggerInventoryLevel;
import buildcraft.core.statements.TriggerMachine;
import buildcraft.core.statements.TriggerRedstoneInput;
import buildcraft.core.tablet.ItemTablet;
import buildcraft.core.tablet.PacketTabletMessage;
import buildcraft.core.tablet.TabletProgramMenuFactory;
import buildcraft.core.tablet.manager.TabletManagerClient;
import buildcraft.core.tablet.manager.TabletManagerServer;

@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", acceptedMinecraftVersions = "[1.7.10,1.8)", dependencies = "required-after:Forge@[10.13.2.1236,)", guiFactory = "buildcraft.core.config.ConfigManager")
public class BuildCraftCore extends BuildCraftMod {
	@Mod.Instance("BuildCraft|Core")
	public static BuildCraftCore instance;

	public static final boolean DEVELOPER_MODE = false;

	public enum RenderMode {
		Full, NoDynamic
	}

	public static RootCommand commandBuildcraft = new RootCommand("buildcraft");
	public static XorShift128Random random = new XorShift128Random();
	public static RenderMode render = RenderMode.Full;
	public static boolean debugWorldgen = false;
	public static boolean modifyWorld = false;
	public static boolean colorBlindMode = false;
	public static boolean hidePowerNumbers = false;
	public static boolean hideFluidNumbers = false;
	public static boolean canEnginesExplode = false;
	public static boolean useServerDataOnClient = true;
	public static boolean alphaPassBugPresent = true;
	public static int itemLifespan = 1200;
	public static int updateFactor = 10;
	public static int builderMaxPerItemFactor = 1024;
	public static long longUpdateFactor = 40;
	public static BuildCraftConfiguration mainConfiguration;
	public static ConfigManager mainConfigManager;

	public static BlockEngine engineBlock;
	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static Block springBlock;
	public static BlockBuildTool buildToolBlock;
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;
	public static Item wrenchItem;
	public static Item mapLocationItem;
	public static Item debuggerItem;
	public static Item paintbrushItem;
	public static ItemList listItem;
	public static ItemTablet tabletItem;
	@SideOnly(Side.CLIENT)
	public static IIcon redLaserTexture;
	@SideOnly(Side.CLIENT)
	public static IIcon blueLaserTexture;
	@SideOnly(Side.CLIENT)
	public static IIcon stripesLaserTexture;
	@SideOnly(Side.CLIENT)
	public static IIcon transparentTexture;
	@SideOnly(Side.CLIENT)
	public static IIconProvider iconProvider;
	public static int blockByEntityModel;
	public static int complexBlockModel;
	public static int markerModel;
	public static ITriggerExternal triggerMachineActive = new TriggerMachine(true);
	public static ITriggerExternal triggerMachineInactive = new TriggerMachine(false);
	public static IStatement triggerEnergyHigh = new TriggerEnergy(true);
	public static IStatement triggerEnergyLow = new TriggerEnergy(false);
	public static ITriggerExternal triggerEmptyInventory = new TriggerInventory(TriggerInventory.State.Empty);
	public static ITriggerExternal triggerContainsInventory = new TriggerInventory(TriggerInventory.State.Contains);
	public static ITriggerExternal triggerSpaceInventory = new TriggerInventory(TriggerInventory.State.Space);
	public static ITriggerExternal triggerFullInventory = new TriggerInventory(TriggerInventory.State.Full);
	public static ITriggerExternal triggerEmptyFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Empty);
	public static ITriggerExternal triggerContainsFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Contains);
	public static ITriggerExternal triggerSpaceFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Space);
	public static ITriggerExternal triggerFullFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Full);
	public static ITriggerInternal triggerRedstoneActive = new TriggerRedstoneInput(true);
	public static ITriggerInternal triggerRedstoneInactive = new TriggerRedstoneInput(false);
	public static ITriggerExternal triggerInventoryBelow25 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW25);
	public static ITriggerExternal triggerInventoryBelow50 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW50);
	public static ITriggerExternal triggerInventoryBelow75 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW75);
	public static ITriggerExternal triggerFluidContainerBelow25 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW25);
	public static ITriggerExternal triggerFluidContainerBelow50 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW50);
	public static ITriggerExternal triggerFluidContainerBelow75 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW75);
	public static IActionInternal actionRedstone = new ActionRedstoneOutput();
	public static IActionExternal[] actionControl;

	public static boolean loadDefaultRecipes = true;
	public static boolean consumeWaterSources = false;
	public static boolean miningAllowPlayerProtectedBlocks = false;
	public static float miningMultiplier;

	public static AchievementManager achievementManager;

	public static Achievement woodenGearAchievement;
	public static Achievement stoneGearAchievement;
	public static Achievement ironGearAchievement;
	public static Achievement goldGearAchievement;
	public static Achievement diamondGearAchievement;
	public static Achievement wrenchAchievement;
	public static Achievement engineRedstoneAchievement;

	public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		BCLog.logger.info("Starting BuildCraft " + Version.getVersion());
		BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2017");
		BCLog.logger.info("http://www.mod-buildcraft.com");

		commandBuildcraft.addAlias("bc");
		commandBuildcraft.addChildCommand(new SubCommandVersion());
		commandBuildcraft.addChildCommand(new SubCommandChangelog());
		commandBuildcraft.addChildCommand(new SubCommandDeop());
		commandBuildcraft.addChildCommand(new SubCommandOp());

		BuildcraftRecipeRegistry.assemblyTable = AssemblyRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.integrationTable = IntegrationRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.refinery = RefineryRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.programmingTable = ProgrammingRecipeManager.INSTANCE;

		BuilderAPI.schematicHelper = SchematicHelper.INSTANCE;
		BuilderAPI.schematicRegistry = SchematicRegistry.INSTANCE;

		BCRegistry.INSTANCE.setRegistryConfig(new File(evt.getModConfigurationDirectory(), "buildcraft/objects.cfg"));

		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.cfg"));
		mainConfigManager = new ConfigManager(mainConfiguration);
		mainConfiguration.load();

		mainConfigManager.getCat("debug").setShowInGui(false);
		mainConfigManager.getCat("vars").setShowInGui(false);

		mainConfigManager.register("general.useServerDataOnClient", BuildCraftCore.useServerDataOnClient, "Allows BuildCraft to use the integrated server's data on the client on singleplayer worlds. Disable if you're getting the odd crash caused by it.", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("general.builderMaxIterationsPerItemFactor", BuildCraftCore.builderMaxPerItemFactor, "Lower this number if BuildCraft builders/fillers are causing TPS lag. Raise it if you think they are being too slow.", ConfigManager.RestartRequirement.NONE);

		mainConfigManager.register("general.miningBreaksPlayerProtectedBlocks", false, "Should BuildCraft miners be allowed to break blocks using player-specific protection?", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("general.updateCheck", true, "Should I check the BuildCraft version on startup?", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("display.hidePowerValues", false, "Should all power values (RF, RF/t) be hidden?", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("display.hideFluidValues", false, "Should all fluid values (mB, mB/t) be hidden?", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("general.itemLifespan", 60, "How long, in seconds, should items stay on the ground? (Vanilla = 300, default = 60)", ConfigManager.RestartRequirement.NONE)
				.setMinValue(5);
		mainConfigManager.register("network.updateFactor", 10, "How often, in ticks, should network update packets be sent? Increasing this might help network performance.", ConfigManager.RestartRequirement.GAME)
				.setMinValue(1);
		mainConfigManager.register("network.longUpdateFactor", 40, "How often, in ticks, should full network sync packets be sent? Increasing this might help network performance.", ConfigManager.RestartRequirement.GAME)
				.setMinValue(1);
		mainConfigManager.register("general.canEnginesExplode", false, "Should engines explode upon overheat?", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("worldgen.enable", true, "Should BuildCraft generate anything in the world?", ConfigManager.RestartRequirement.GAME);
		mainConfigManager.register("general.pumpsConsumeWater", false, "Should pumps consume water? Enabling this might cause performance issues!", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("power.miningUsageMultiplier", 1.0D, "What should the multiplier of all mining-related power usage be?", ConfigManager.RestartRequirement.NONE);
		mainConfigManager.register("display.colorBlindMode", false, "Should I enable colorblind mode?", ConfigManager.RestartRequirement.GAME);
		mainConfigManager.register("worldgen.generateWaterSprings", true, "Should BuildCraft generate water springs?", ConfigManager.RestartRequirement.GAME);

		reloadConfig(ConfigManager.RestartRequirement.GAME);

		wrenchItem = (new ItemWrench()).setUnlocalizedName("wrenchItem");
		BCRegistry.INSTANCE.registerItem(wrenchItem, false);

		mapLocationItem = (new ItemMapLocation()).setUnlocalizedName("mapLocation");
		BCRegistry.INSTANCE.registerItem(mapLocationItem, false);

		listItem = (ItemList) (new ItemList()).setUnlocalizedName("list");
		BCRegistry.INSTANCE.registerItem(listItem, false);

		debuggerItem = (new ItemDebugger()).setUnlocalizedName("debugger");
		BCRegistry.INSTANCE.registerItem(debuggerItem, false);

		if (BuildCraftCore.modifyWorld) {
			BlockSpring.EnumSpring.WATER.canGen = BuildCraftCore.mainConfigManager.get("worldgen.generateWaterSprings").getBoolean();
			springBlock = new BlockSpring().setBlockName("eternalSpring");
			BCRegistry.INSTANCE.registerBlock(springBlock, ItemSpring.class, false);
		}

		woodenGearItem = (new ItemGear()).setUnlocalizedName("woodenGearItem");
		if (BCRegistry.INSTANCE.registerItem(woodenGearItem, false)) {
			OreDictionary.registerOre("gearWood", new ItemStack(woodenGearItem));
		}

		stoneGearItem = (new ItemGear()).setUnlocalizedName("stoneGearItem");
		if (BCRegistry.INSTANCE.registerItem(stoneGearItem, false)) {
			OreDictionary.registerOre("gearStone", new ItemStack(stoneGearItem));
		}

		ironGearItem = (new ItemGear()).setUnlocalizedName("ironGearItem");
		if (BCRegistry.INSTANCE.registerItem(ironGearItem, false)) {
			OreDictionary.registerOre("gearIron", new ItemStack(ironGearItem));
		}

		goldGearItem = (new ItemGear()).setUnlocalizedName("goldGearItem");
		if (BCRegistry.INSTANCE.registerItem(goldGearItem, false)) {
			OreDictionary.registerOre("gearGold", new ItemStack(goldGearItem));
		}

		diamondGearItem = (new ItemGear()).setUnlocalizedName("diamondGearItem");
		if (BCRegistry.INSTANCE.registerItem(diamondGearItem, false)) {
			OreDictionary.registerOre("gearDiamond", new ItemStack(diamondGearItem));
		}

		paintbrushItem = (new ItemPaintbrush()).setUnlocalizedName("paintbrush");
		BCRegistry.INSTANCE.registerItem(paintbrushItem, false);

		if (DEVELOPER_MODE) {
			tabletItem = new ItemTablet();
			tabletItem.setUnlocalizedName("tablet");
			BCRegistry.INSTANCE.registerItem(tabletItem, false);
		}

		buildToolBlock = new BlockBuildTool();
		buildToolBlock.setBlockName("buildToolBlock");
		BCRegistry.INSTANCE.registerBlock(buildToolBlock, true);

		engineBlock = (BlockEngine) CompatHooks.INSTANCE.getBlock(BlockEngine.class);
		BCRegistry.INSTANCE.registerBlock(engineBlock, ItemEngine.class, true);
		engineBlock.registerTile((Class<? extends TileEngineBase>) CompatHooks.INSTANCE.getTile(TileEngineWood.class), 0, "tile.engineWood", "buildcraftcore:engineWood");
		BCRegistry.INSTANCE.registerTileEntity(TileEngineWood.class, "net.minecraft.src.buildcraft.energy.TileEngineWood");

		markerBlock = (BlockMarker) CompatHooks.INSTANCE.getBlock(BlockMarker.class);
		BCRegistry.INSTANCE.registerBlock(markerBlock.setBlockName("markerBlock"), false);

		pathMarkerBlock = (BlockPathMarker) CompatHooks.INSTANCE.getBlock(BlockPathMarker.class);
		BCRegistry.INSTANCE.registerBlock(pathMarkerBlock.setBlockName("pathMarkerBlock"), false);

		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new BlockHighlightHandler());
		MinecraftForge.EVENT_BUS.register(new ListTooltipHandler());

		OreDictionary.registerOre("chestWood", Blocks.chest);
		OreDictionary.registerOre("craftingTableWood", Blocks.crafting_table);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		BuildCraftAPI.proxy = CoreProxy.proxy;

		ChannelHandler coreChannelHandler = new ChannelHandler();
		coreChannelHandler.registerPacketType(PacketTabletMessage.class);

		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-CORE", coreChannelHandler, new PacketHandlerCore());

		achievementManager = new AchievementManager("BuildCraft");
		FMLCommonHandler.instance().bus().register(achievementManager);

		woodenGearAchievement = achievementManager.registerAchievement(new Achievement("achievement.woodenGear", "woodenGearAchievement", 0, 0, woodenGearItem, null));
		stoneGearAchievement = achievementManager.registerAchievement(new Achievement("achievement.stoneGear", "stoneGearAchievement", 2, 0, stoneGearItem, woodenGearAchievement));
		ironGearAchievement = achievementManager.registerAchievement(new Achievement("achievement.ironGear", "ironGearAchievement", 4, 0, ironGearItem, stoneGearAchievement));
		goldGearAchievement = achievementManager.registerAchievement(new Achievement("achievement.goldGear", "goldGearAchievement", 6, 0, goldGearItem, ironGearAchievement));
		diamondGearAchievement = achievementManager.registerAchievement(new Achievement("achievement.diamondGear", "diamondGearAchievement", 8, 0, diamondGearItem, goldGearAchievement));
		wrenchAchievement = achievementManager.registerAchievement(new Achievement("achievement.wrench", "wrenchAchievement", 3, 2, wrenchItem, stoneGearAchievement));
		engineRedstoneAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.redstoneEngine", "engineAchievement1", 1, -2, new ItemStack(engineBlock, 1, 0), BuildCraftCore.woodenGearAchievement));

		// BuildCraft 6.1.4 and below - migration only
		StatementManager.registerParameterClass("buildcraft:stackTrigger", StatementParameterItemStack.class);
		StatementManager.registerParameterClass("buildcraft:stackAction", StatementParameterItemStack.class);

		StatementManager.registerParameterClass(StatementParameterItemStack.class);
		StatementManager.registerParameterClass(StatementParameterItemStackExact.class);
		StatementManager.registerParameterClass(StatementParameterDirection.class);
		StatementManager.registerParameterClass(StatementParameterRedstoneGateSideOnly.class);
		StatementManager.registerTriggerProvider(new DefaultTriggerProvider());
		StatementManager.registerActionProvider(new DefaultActionProvider());

		if (BuildCraftCore.modifyWorld) {
			MinecraftForge.EVENT_BUS.register(new SpringPopulate());
		}

		if (mainConfiguration.hasChanged()) {
			mainConfiguration.save();
		}

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		if (BCCreativeTab.isPresent("main")) {
			BCCreativeTab.get("main").setIcon(new ItemStack(BuildCraftCore.wrenchItem, 1));
		}

		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

		BuilderAPI.schematicRegistry.registerSchematicBlock(engineBlock, SchematicEngine.class);

		CoreProxy.proxy.initializeRendering();
		CoreProxy.proxy.initializeEntityRendering();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new CoreGuiHandler());

		FMLCommonHandler.instance().bus().register(TabletManagerClient.INSTANCE);
		FMLCommonHandler.instance().bus().register(TabletManagerServer.INSTANCE);
		MinecraftForge.EVENT_BUS.register(TabletManagerClient.INSTANCE);
		MinecraftForge.EVENT_BUS.register(TabletManagerServer.INSTANCE);

		TabletAPI.registerProgram(new TabletProgramMenuFactory());

		// Create filler registry
		try {
			FillerManager.registry = new FillerRegistry();

			// INIT FILLER PATTERNS
			FillerManager.registry.addPattern(PatternFill.INSTANCE);
			FillerManager.registry.addPattern(new PatternFlatten());
			FillerManager.registry.addPattern(new PatternHorizon());
			FillerManager.registry.addPattern(new PatternClear());
			FillerManager.registry.addPattern(new PatternBox());
			FillerManager.registry.addPattern(new PatternPyramid());
			FillerManager.registry.addPattern(new PatternStairs());
			FillerManager.registry.addPattern(new PatternCylinder());
			FillerManager.registry.addPattern(new PatternFrame());
		} catch (Error error) {
			BCLog.logErrorAPI(error, IFillerPattern.class);
			throw error;
		}

		StatementManager.registerParameterClass(PatternParameterYDir.class);
		StatementManager.registerParameterClass(PatternParameterXZDir.class);
		StatementManager.registerParameterClass(PatternParameterCenter.class);
		StatementManager.registerParameterClass(PatternParameterHollow.class);

		ListRegistry.registerHandler(new ListMatchHandlerClass());
		ListRegistry.registerHandler(new ListMatchHandlerFluid());
		ListRegistry.registerHandler(new ListMatchHandlerTools());
		ListRegistry.registerHandler(new ListMatchHandlerArmor());
		ListRegistry.itemClassAsType.add(ItemFood.class);

		if (Loader.isModLoaded("foamfix")) {
			alphaPassBugPresent = false;
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		BCLog.logger.info("BuildCraft's fake player: UUID = " + gameProfile.getId().toString() + ", name = '" + gameProfile.getName() + "'!");

		BCRegistry.INSTANCE.save();

		for (Object o : Block.blockRegistry) {
			Block block = (Block) o;

			if (block instanceof BlockFluidBase || block instanceof BlockLiquid || block instanceof IPlantable) {
				BuildCraftAPI.softBlocks.add(block);
			}
		}

		BuildCraftAPI.softBlocks.add(Blocks.snow);
		BuildCraftAPI.softBlocks.add(Blocks.vine);
		BuildCraftAPI.softBlocks.add(Blocks.fire);

		FMLCommonHandler.instance().bus().register(new TickHandlerCore());

		CropManager.setDefaultHandler(new CropHandlerPlantable());
		CropManager.registerHandler(new CropHandlerReeds());

		CropHandlerPlantable.forbidBlock(Blocks.reeds);

		BuildCraftAPI.registerWorldProperty("replaceable", new WorldPropertyIsReplaceable());
		BuildCraftAPI.registerWorldProperty("soft", new WorldPropertyIsSoft());
		BuildCraftAPI.registerWorldProperty("wood", new WorldPropertyIsWood());
		BuildCraftAPI.registerWorldProperty("leaves", new WorldPropertyIsLeaf());
		for (int i = 0; i < 4; i++) {
			BuildCraftAPI.registerWorldProperty("ore@hardness=" + i, new WorldPropertyIsOre(i));
		}
		BuildCraftAPI.registerWorldProperty("harvestable", new WorldPropertyIsHarvestable());
		BuildCraftAPI.registerWorldProperty("farmland", new WorldPropertyIsFarmland());
		BuildCraftAPI.registerWorldProperty("shoveled", new WorldPropertyIsShoveled());
		BuildCraftAPI.registerWorldProperty("dirt", new WorldPropertyIsDirt());
		BuildCraftAPI.registerWorldProperty("fluidSource", new WorldPropertyIsFluidSource());

		// Landmarks are often caught incorrectly, making building them counter-productive.
		SchematicRegistry.INSTANCE.registerSchematicBlock(markerBlock, SchematicIgnore.class);
		SchematicRegistry.INSTANCE.registerSchematicBlock(pathMarkerBlock, SchematicIgnore.class);

		ColorUtils.initialize();

		actionControl = new IActionExternal[IControllable.Mode.values().length];
		for (IControllable.Mode mode : IControllable.Mode.values()) {
			if (mode != IControllable.Mode.Unknown && mode != IControllable.Mode.Mode) {
				actionControl[mode.ordinal()] = new ActionMachineControl(mode);
			}
		}

		MinecraftForge.EVENT_BUS.register(ListOreDictionaryCache.INSTANCE);
		for (String s : OreDictionary.getOreNames()) {
			ListOreDictionaryCache.INSTANCE.registerName(s);
		}

		ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(commandBuildcraft);

		/* Increase the builder speed in singleplayer mode.
		   Singleplayer users generally run far fewer of them. */

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			BuildingSlotMapIterator.MAX_PER_ITEM = builderMaxPerItemFactor * 4;
		} else {
			BuildingSlotMapIterator.MAX_PER_ITEM = builderMaxPerItemFactor;
		}

		if (Utils.CAULDRON_DETECTED) {
			BCLog.logger.warn("############################################");
			BCLog.logger.warn("#                                          #");
			BCLog.logger.warn("#  Cauldron has been detected! Please keep #");
			BCLog.logger.warn("#  in mind that BuildCraft may NOT provide #");
			BCLog.logger.warn("# support to Cauldron users, as the mod is #");
			BCLog.logger.warn("# primarily tested without Bukkit/Spigot's #");
			BCLog.logger.warn("#    changes to the Minecraft internals.   #");
			BCLog.logger.warn("#                                          #");
			BCLog.logger.warn("#  Any lag caused by BuildCraft on top of  #");
			BCLog.logger.warn("#  Cauldron likely arises from workarounds #");
			BCLog.logger.warn("#  which we apply to make sure BuildCraft  #");
			BCLog.logger.warn("#  works properly with Cauldron installed. #");
			BCLog.logger.warn("#                                          #");
			BCLog.logger.warn("#     Thanks for your attention! ~ BC devs #");
			BCLog.logger.warn("#                                          #");
			BCLog.logger.warn("############################################");

			// To people reading that code and thinking we're lying:
			// Cauldron does not invalidate tile entities properly, causing
			// issues with our tile entity cache. That is the bug and that
			// is also the reason for the extra lag caused when using Cauldron.
		}
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		TabletManagerClient.INSTANCE.onServerStopping();
		TabletManagerServer.INSTANCE.onServerStopping();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		for (FillerPattern pattern : FillerPattern.patterns.values()) {
			pattern.registerIcons(event.map);
		}

		if (event.map.getTextureType() == 1) {
			iconProvider = new CoreIconProvider();
			iconProvider.registerIcons(event.map);

			StatementManager.registerIcons(event.map);
		} else if (event.map.getTextureType() == 0) {
			BuildCraftCore.redLaserTexture = event.map.registerIcon("buildcraftcore:laserBox/blockRedLaser");
			BuildCraftCore.blueLaserTexture = event.map.registerIcon("buildcraftcore:laserBox/blockBlueLaser");
			BuildCraftCore.stripesLaserTexture = event.map.registerIcon("buildcraftcore:laserBox/blockStripesLaser");
			BuildCraftCore.transparentTexture = event.map.registerIcon("buildcraftcore:misc/transparent");
			RenderLEDTile.registerBlockIcons(event.map);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event) {
		FluidRenderer.onTextureReload();
		RenderLaser.onTextureReload();
	}

	public void reloadConfig(ConfigManager.RestartRequirement restartType) {
		if (restartType == ConfigManager.RestartRequirement.GAME) {
			modifyWorld = mainConfigManager.get("worldgen.enable").getBoolean();
			updateFactor = mainConfigManager.get("network.updateFactor").getInt();
			longUpdateFactor = mainConfigManager.get("network.longUpdateFactor").getInt();
			colorBlindMode = mainConfigManager.get("display.colorBlindMode").getBoolean();

			reloadConfig(ConfigManager.RestartRequirement.WORLD);
		} else if (restartType == ConfigManager.RestartRequirement.WORLD) {
			reloadConfig(ConfigManager.RestartRequirement.NONE);
		} else {
			useServerDataOnClient = mainConfigManager.get("general.useServerDataOnClient").getBoolean(true);
			builderMaxPerItemFactor = mainConfigManager.get("general.builderMaxIterationsPerItemFactor").getInt();
			hideFluidNumbers = mainConfigManager.get("display.hideFluidValues").getBoolean();
			hidePowerNumbers = mainConfigManager.get("display.hidePowerValues").getBoolean();
			itemLifespan = mainConfigManager.get("general.itemLifespan").getInt();
			canEnginesExplode = mainConfigManager.get("general.canEnginesExplode").getBoolean();
			consumeWaterSources = mainConfigManager.get("general.pumpsConsumeWater").getBoolean();
			miningMultiplier = (float) mainConfigManager.get("power.miningUsageMultiplier").getDouble();
			miningAllowPlayerProtectedBlocks = mainConfigManager.get("general.miningBreaksPlayerProtectedBlocks").getBoolean();

			BuildingSlotMapIterator.MAX_PER_ITEM = builderMaxPerItemFactor;

			if (miningMultiplier <= 0) {
				throw new RuntimeException("Please do not set the miningMultiplier to values <= 0.0!");
			}

			if (mainConfigManager.get("general.updateCheck").getBoolean(true)) {
				Version.check();
			}

			if (mainConfiguration.hasChanged()) {
				mainConfiguration.save();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if ("BuildCraft|Core".equals(event.modID)) {
			reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
		}
	}

	public void loadRecipes() {
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", 'I', "ingotIron", 'G', "gearStone");
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(woodenGearItem), " S ", "S S",
				" S ", 'S',
				"stickWood");
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(stoneGearItem), " I ", "IGI",
				" I ", 'I',
				"cobblestone", 'G',
				"gearWood");
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(ironGearItem), " I ", "IGI",
				" I ", 'I',
				"ingotIron", 'G', "gearStone");
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(goldGearItem), " I ", "IGI",
				" I ", 'I',
				"ingotGold", 'G', "gearIron");
		BCRegistry.INSTANCE.addCraftingRecipe(
				new ItemStack(diamondGearItem), " I ", "IGI", " I ", 'I', "gemDiamond", 'G', "gearGold");
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(mapLocationItem), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y', "dyeYellow");

		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(engineBlock, 1, 0),
				"www", " g ", "GpG", 'w', "plankWood", 'g', "blockGlass", 'G',
				"gearWood", 'p', Blocks.piston);

		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(markerBlock, 1), "l ", "r ", 'l',
				new ItemStack(Items.dye, 1, 4), 'r', Blocks.redstone_torch);

		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), "l ", "r ", 'l',
				"dyeGreen", 'r', Blocks.redstone_torch);

		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(paintbrushItem), " iw", " gi", "s  ",
				's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.wool, 1, 0), 'i', Items.string);

		ItemStack anyPaintbrush = new ItemStack(paintbrushItem, 1, OreDictionary.WILDCARD_VALUE);

		for (int i = 0; i < 16; i++) {
			ItemStack outputStack = new ItemStack(paintbrushItem);
			NBTUtils.getItemData(outputStack).setByte("color", (byte) i);
			BCRegistry.INSTANCE.addShapelessRecipe(outputStack, anyPaintbrush, EnumColor.fromId(i).getDye());
		}

		// Convert old lists to new lists
		BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(listItem, 1, 1), new ItemStack(listItem, 1, 0));

		if (Loader.isModLoaded("BuildCraft|Silicon")) {
			CoreSiliconRecipes.loadSiliconRecipes();
		} else {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(listItem, 1, 1), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y',
					"dyeGreen");
		}
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@SubscribeEvent
	public void cleanRegistries(WorldEvent.Unload event) {
		for (IWorldProperty property : BuildCraftAPI.worldProperties.values()) {
			property.clear();
		}
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TilePathMarker.clearAvailableMarkersList(event.world);
		}
	}
}
