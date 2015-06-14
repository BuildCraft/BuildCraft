/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
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

import net.minecraftforge.client.event.RenderWorldLastEvent;
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
import buildcraft.core.BlockBuildTool;
import buildcraft.core.BlockEngine;
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
import buildcraft.core.Version;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.patterns.FillerRegistry;
import buildcraft.core.builders.patterns.PatternBox;
import buildcraft.core.builders.patterns.PatternClear;
import buildcraft.core.builders.patterns.PatternCylinder;
import buildcraft.core.builders.patterns.PatternFill;
import buildcraft.core.builders.patterns.PatternFlatten;
import buildcraft.core.builders.patterns.PatternFrame;
import buildcraft.core.builders.patterns.PatternHorizon;
import buildcraft.core.builders.patterns.PatternParameterXZDir;
import buildcraft.core.builders.patterns.PatternParameterYDir;
import buildcraft.core.builders.patterns.PatternPyramid;
import buildcraft.core.builders.patterns.PatternStairs;
import buildcraft.core.command.SubCommandChangelog;
import buildcraft.core.command.SubCommandVersion;
import buildcraft.core.config.BuildCraftConfiguration;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.crops.CropHandlerPlantable;
import buildcraft.core.crops.CropHandlerReeds;
import buildcraft.core.lib.commands.RootCommand;
import buildcraft.core.lib.engines.ItemEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.XorShift128Random;
import buildcraft.core.network.PacketHandlerCore;
import buildcraft.core.properties.WorldPropertyIsDirt;
import buildcraft.core.properties.WorldPropertyIsFarmland;
import buildcraft.core.properties.WorldPropertyIsFluidSource;
import buildcraft.core.properties.WorldPropertyIsHarvestable;
import buildcraft.core.properties.WorldPropertyIsLeaf;
import buildcraft.core.properties.WorldPropertyIsOre;
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
import buildcraft.core.statements.ActionMachineControl;
import buildcraft.core.statements.ActionRedstoneOutput;
import buildcraft.core.statements.DefaultActionProvider;
import buildcraft.core.statements.DefaultTriggerProvider;
import buildcraft.core.statements.StatementParameterDirection;
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

	public static final boolean NONRELEASED_BLOCKS = true;
	public static final boolean TABLET_TESTING = false;

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
	public static int itemLifespan = 1200;
	public static int updateFactor = 10;
	public static long longUpdateFactor = 40;
	public static BuildCraftConfiguration mainConfiguration;
	public static ConfigManager mainConfigManager;

	public static BlockEngine engineBlock;
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
	public static int legacyPipeModel;
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
	public static float miningMultiplier;

	public static AchievementManager achievementManager;

	public static Achievement woodenGearAchievement;
	public static Achievement stoneGearAchievement;
	public static Achievement ironGearAchievement;
	public static Achievement goldGearAchievement;
	public static Achievement diamondGearAchievement;
	public static Achievement wrenchAchievement;
	public static Achievement engineRedstoneAchievement;

	public static float diffX, diffY, diffZ;

	public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

	private static FloatBuffer modelviewF;
	private static FloatBuffer projectionF;
	private static IntBuffer viewport;

	private static FloatBuffer pos = ByteBuffer.allocateDirect(3 * 4).asFloatBuffer();

	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		BCLog.initLog();

		new BCCreativeTab("main");

		commandBuildcraft.addAlias("bc");
		commandBuildcraft.addChildCommand(new SubCommandVersion());
		commandBuildcraft.addChildCommand(new SubCommandChangelog());

		BuildcraftRecipeRegistry.assemblyTable = AssemblyRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.integrationTable = IntegrationRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.refinery = RefineryRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.programmingTable = ProgrammingRecipeManager.INSTANCE;

		BuilderAPI.schematicRegistry = SchematicRegistry.INSTANCE;
		
		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.cfg"));
		mainConfigManager = new ConfigManager(mainConfiguration);
		try {
			mainConfiguration.load();

			mainConfigManager.getCat("debug").setShowInGui(false);
			mainConfigManager.getCat("vars").setShowInGui(false);

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
			CoreProxy.proxy.registerItem(wrenchItem);

			mapLocationItem = (new ItemMapLocation()).setUnlocalizedName("mapLocation");
			CoreProxy.proxy.registerItem(mapLocationItem);

			listItem = (ItemList) ((new ItemList()).setUnlocalizedName("list"));
			CoreProxy.proxy.registerItem(listItem);

			debuggerItem = (ItemDebugger) ((new ItemDebugger())).setUnlocalizedName("debugger");
			CoreProxy.proxy.registerItem(debuggerItem);

			if (BuildCraftCore.modifyWorld) {
				BlockSpring.EnumSpring.WATER.canGen = BuildCraftCore.mainConfigManager.get("worldgen.generateWaterSprings").getBoolean();
				springBlock = new BlockSpring().setBlockName("eternalSpring");
				CoreProxy.proxy.registerBlock(springBlock, ItemSpring.class);
			}

			woodenGearItem = (new ItemGear()).setUnlocalizedName("woodenGearItem");
			CoreProxy.proxy.registerItem(woodenGearItem);
			OreDictionary.registerOre("gearWood", new ItemStack(woodenGearItem));

			stoneGearItem = (new ItemGear()).setUnlocalizedName("stoneGearItem");
			CoreProxy.proxy.registerItem(stoneGearItem);
			OreDictionary.registerOre("gearStone", new ItemStack(stoneGearItem));

			ironGearItem = (new ItemGear()).setUnlocalizedName("ironGearItem");
			CoreProxy.proxy.registerItem(ironGearItem);
			OreDictionary.registerOre("gearIron", new ItemStack(ironGearItem));

			goldGearItem = (new ItemGear()).setUnlocalizedName("goldGearItem");
			CoreProxy.proxy.registerItem(goldGearItem);
			OreDictionary.registerOre("gearGold", new ItemStack(goldGearItem));

			diamondGearItem = (new ItemGear()).setUnlocalizedName("diamondGearItem");
			CoreProxy.proxy.registerItem(diamondGearItem);
			OreDictionary.registerOre("gearDiamond", new ItemStack(diamondGearItem));

			paintbrushItem = (new ItemPaintbrush()).setUnlocalizedName("paintbrush");
			CoreProxy.proxy.registerItem(paintbrushItem);

			if (TABLET_TESTING) {
				tabletItem = new ItemTablet();
				tabletItem.setUnlocalizedName("tablet");
				CoreProxy.proxy.registerItem(tabletItem);
			}

			buildToolBlock = new BlockBuildTool();
			buildToolBlock.setBlockName("buildToolBlock");
			CoreProxy.proxy.registerBlock(buildToolBlock);

			engineBlock = (BlockEngine) CompatHooks.INSTANCE.getBlock(BlockEngine.class);
			CoreProxy.proxy.registerBlock(engineBlock, ItemEngine.class);
			engineBlock.registerTile((Class<? extends TileEngineBase>) CompatHooks.INSTANCE.getTile(TileEngineWood.class), "tile.engineWood");
			CoreProxy.proxy.registerTileEntity(TileEngineWood.class, "net.minecraft.src.buildcraft.energy.TileEngineWood");

			FMLCommonHandler.instance().bus().register(this);
			MinecraftForge.EVENT_BUS.register(this);
			MinecraftForge.EVENT_BUS.register(new BlockHighlightHandler());
		} finally {
		}

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

		BCCreativeTab.get("main").setIcon(new ItemStack(BuildCraftCore.wrenchItem, 1));

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
			BCLog.logErrorAPI("Buildcraft", error, IFillerPattern.class);
			throw error;
		}

		StatementManager.registerParameterClass(PatternParameterYDir.class);
		StatementManager.registerParameterClass(PatternParameterXZDir.class);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		BCLog.logger.info("BuildCraft's fake player: UUID = " + gameProfile.getId().toString() + ", name = '" + gameProfile.getName() + "'!");

		for (Object o : Block.blockRegistry) {
			Block block = (Block) o;

			if (block instanceof BlockFluidBase || block instanceof BlockLiquid || block instanceof IPlantable) {
				BuildCraftAPI.softBlocks.add(block);
			}
		}

		BuildCraftAPI.softBlocks.add(Blocks.snow);
		BuildCraftAPI.softBlocks.add(Blocks.vine);
		BuildCraftAPI.softBlocks.add(Blocks.fire);
		BuildCraftAPI.softBlocks.add(Blocks.air);

		FMLCommonHandler.instance().bus().register(new TickHandlerCore());

		CropManager.setDefaultHandler(new CropHandlerPlantable());
		CropManager.registerHandler(new CropHandlerReeds());

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
		
		ColorUtils.initialize();
		
		actionControl = new IActionExternal[IControllable.Mode.values().length];
		for (IControllable.Mode mode : IControllable.Mode.values()) {
			if (mode != IControllable.Mode.Unknown && mode != IControllable.Mode.Mode) {
				actionControl[mode.ordinal()] = new ActionMachineControl(mode);
			}
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(commandBuildcraft);

		if (Utils.CAULDRON_DETECTED) {
			BCLog.logger.warn("############################################");
			BCLog.logger.warn("#                                          #");
			BCLog.logger.warn("#  Cauldron has been detected! Please keep #");
			BCLog.logger.warn("# in mind that BuildCraft does NOT support #");
			BCLog.logger.warn("#   Cauldron and we do not promise to fix  #");
			BCLog.logger.warn("#  bugs caused by its modifications to the #");
			BCLog.logger.warn("#   Minecraft engine. Please reconsider.   #");
			BCLog.logger.warn("#                                          #");
			BCLog.logger.warn("#  Any lag caused by BuildCraft on top of  #");
			BCLog.logger.warn("# Cauldron likely arises from our fixes to #");
			BCLog.logger.warn("#  their bugs, so please don't report that #");
			BCLog.logger.warn("#  either. Thanks for your attention! ~BC  #");
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
		if (event.map.getTextureType() == 1) {
			iconProvider = new CoreIconProvider();
			iconProvider.registerIcons(event.map);
		} else if (event.map.getTextureType() == 0) {
			BuildCraftCore.redLaserTexture = event.map.registerIcon("buildcraftcore:laserBox/blockRedLaser");
			BuildCraftCore.blueLaserTexture = event.map.registerIcon("buildcraftcore:laserBox/blockBlueLaser");
			BuildCraftCore.stripesLaserTexture = event.map.registerIcon("buildcraftcore:laserBox/blockStripesLaser");
			BuildCraftCore.transparentTexture = event.map.registerIcon("buildcraftcore:misc/transparent");
			RenderLEDTile.registerBlockIcons(event.map);
		}
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
			hideFluidNumbers = mainConfigManager.get("display.hideFluidValues").getBoolean();
			hidePowerNumbers = mainConfigManager.get("display.hidePowerValues").getBoolean();
			itemLifespan = mainConfigManager.get("general.itemLifespan").getInt();
			canEnginesExplode = mainConfigManager.get("general.canEnginesExplode").getBoolean();
			consumeWaterSources = mainConfigManager.get("general.pumpsConsumeWater").getBoolean();
			miningMultiplier = (float) mainConfigManager.get("power.miningUsageMultiplier").getDouble();

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
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", 'I', "ingotIron", 'G', "gearStone");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(woodenGearItem), " S ", "S S",
				" S ", 'S',
				"stickWood");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(stoneGearItem), " I ", "IGI",
				" I ", 'I',
				"cobblestone", 'G',
				"gearWood");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(ironGearItem), " I ", "IGI",
				" I ", 'I',
				"ingotIron", 'G', "gearStone");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(goldGearItem), " I ", "IGI",
				" I ", 'I',
				"ingotGold", 'G', "gearIron");
		CoreProxy.proxy.addCraftingRecipe(
				new ItemStack(diamondGearItem), " I ", "IGI", " I ", 'I', "gemDiamond", 'G', "gearGold");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(mapLocationItem), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y', "dyeYellow");

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 0),
				"www", " g ", "GpG", 'w', "plankWood", 'g', "blockGlass", 'G',
				"gearWood", 'p', Blocks.piston);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(paintbrushItem), " iw", " gi", "s  ",
				's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.wool, 1, 0), 'i', Items.string);

		for (int i = 0; i < 16; i++) {
			ItemStack outputStack = new ItemStack(paintbrushItem);
			NBTUtils.getItemData(outputStack).setByte("color", (byte) i);
			CoreProxy.proxy.addShapelessRecipe(outputStack, paintbrushItem, EnumColor.fromId(i).getDye());
		}

		if (Loader.isModLoaded("BuildCraft|Silicon")) {
			CoreSiliconRecipes.loadSiliconRecipes();
		} else {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(listItem), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y',
					"dyeGreen");
		}
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderLast (RenderWorldLastEvent evt) {
		// TODO: while the urbanist is deactivated, this code can be dormant.
		// it happens to be very expensive at run time, so we need some way
		// to operate it only when relevant (e.g. in the cycle following a
		// click request).
		if (NONRELEASED_BLOCKS) {
			return;
		}

		/**
		 * Note (SpaceToad): Why on earth this thing eventually worked out is a
		 * mystery to me. In particular, all the examples I got computed y in
		 * a different way. Anyone with further OpenGL understanding would be
		 * welcome to explain.
		 *
		 * Anyway, the purpose of this code is to store the block position
		 * pointed by the mouse at each frame, relative to the entity that has
		 * the camera.
		 *
		 * It got heavily inspire from the two following sources:
		 * http://nehe.gamedev.net/article/using_gluunproject/16013/
		 * #ActiveRenderInfo.updateRenderInfo.
		 *
		 * See EntityUrbanist#rayTraceMouse for a usage example.
		 */

		if (modelviewF == null) {
			modelviewF = GLAllocation.createDirectFloatBuffer(16);
			projectionF = GLAllocation.createDirectFloatBuffer(16);
			viewport = GLAllocation.createDirectIntBuffer(16);

		}

		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelviewF);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionF);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
		float f = (viewport.get(0) + viewport.get(2)) / 2;
		float f1 = (viewport.get(1) + viewport.get(3)) / 2;

		float x = Mouse.getX();
		float y = Mouse.getY();

		// TODO: Minecraft seems to instist to have this winZ re-created at
		// each frame - looks like a memory leak to me but I couldn't use a
		// static variable instead, as for the rest.
		FloatBuffer winZ = GLAllocation.createDirectFloatBuffer(1);
		GL11.glReadPixels((int) x, (int) y, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, winZ);

		GLU.gluUnProject(x, y, winZ.get(), modelviewF, projectionF, viewport,
				pos);

		diffX = pos.get(0);
		diffY = pos.get(1);
		diffZ = pos.get(2);
	}

	@SubscribeEvent
	public void cleanRegistries(WorldEvent.Unload unload) {
		for (IWorldProperty property : BuildCraftAPI.worldProperties.values()) {
			property.clear();
		}
	}
}
