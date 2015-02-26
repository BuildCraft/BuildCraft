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
import java.util.HashSet;
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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.core.JavaTools;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.BlockSpring;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.CommandBuildCraft;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiHandler;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemGear;
import buildcraft.core.ItemList;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.ItemSpring;
import buildcraft.core.ItemWrench;
import buildcraft.core.SpringPopulate;
import buildcraft.core.TickHandlerCore;
import buildcraft.core.Version;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.IntegrationRecipeManager;
import buildcraft.core.recipes.ProgrammingRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.render.BlockHighlightHandler;
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
import buildcraft.core.utils.ColorUtils;
import buildcraft.core.utils.CraftingHandler;
import buildcraft.core.utils.WorldPropertyIsDirt;
import buildcraft.core.utils.WorldPropertyIsFarmland;
import buildcraft.core.utils.WorldPropertyIsFluidSource;
import buildcraft.core.utils.WorldPropertyIsHarvestable;
import buildcraft.core.utils.WorldPropertyIsLeaf;
import buildcraft.core.utils.WorldPropertyIsOre;
import buildcraft.core.utils.WorldPropertyIsShoveled;
import buildcraft.core.utils.WorldPropertyIsSoft;
import buildcraft.core.utils.WorldPropertyIsWood;
import buildcraft.energy.fuels.CoolantManager;
import buildcraft.energy.fuels.FuelManager;
import buildcraft.robots.EntityRobot;
import buildcraft.robots.ResourceIdAssemblyTable;
import buildcraft.robots.ResourceIdBlock;
import buildcraft.robots.ResourceIdRequest;
import buildcraft.robots.ai.AIRobotAttack;
import buildcraft.robots.ai.AIRobotBreak;
import buildcraft.robots.ai.AIRobotCraftAssemblyTable;
import buildcraft.robots.ai.AIRobotCraftFurnace;
import buildcraft.robots.ai.AIRobotCraftWorkbench;
import buildcraft.robots.ai.AIRobotDeliverRequested;
import buildcraft.robots.ai.AIRobotDisposeItems;
import buildcraft.robots.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robots.ai.AIRobotFetchItem;
import buildcraft.robots.ai.AIRobotGoAndLinkToDock;
import buildcraft.robots.ai.AIRobotGoto;
import buildcraft.robots.ai.AIRobotGotoBlock;
import buildcraft.robots.ai.AIRobotGotoSleep;
import buildcraft.robots.ai.AIRobotGotoStation;
import buildcraft.robots.ai.AIRobotGotoStationAndLoad;
import buildcraft.robots.ai.AIRobotGotoStationAndLoadFluids;
import buildcraft.robots.ai.AIRobotGotoStationAndUnload;
import buildcraft.robots.ai.AIRobotGotoStationToLoad;
import buildcraft.robots.ai.AIRobotGotoStationToLoadFluids;
import buildcraft.robots.ai.AIRobotGotoStationToUnload;
import buildcraft.robots.ai.AIRobotGotoStationToUnloadFluids;
import buildcraft.robots.ai.AIRobotLoad;
import buildcraft.robots.ai.AIRobotLoadFluids;
import buildcraft.robots.ai.AIRobotMain;
import buildcraft.robots.ai.AIRobotPumpBlock;
import buildcraft.robots.ai.AIRobotRecharge;
import buildcraft.robots.ai.AIRobotSearchAndGotoStation;
import buildcraft.robots.ai.AIRobotSearchBlock;
import buildcraft.robots.ai.AIRobotSearchEntity;
import buildcraft.robots.ai.AIRobotSearchRandomBlock;
import buildcraft.robots.ai.AIRobotSearchRandomGroundBlock;
import buildcraft.robots.ai.AIRobotSearchStackRequest;
import buildcraft.robots.ai.AIRobotSearchStation;
import buildcraft.robots.ai.AIRobotSleep;
import buildcraft.robots.ai.AIRobotStraightMoveTo;
import buildcraft.robots.ai.AIRobotUnload;
import buildcraft.robots.ai.AIRobotUnloadFluids;
import buildcraft.robots.ai.AIRobotUseToolOnBlock;
import buildcraft.robots.boards.BoardRobotBomber;
import buildcraft.robots.boards.BoardRobotBuilder;
import buildcraft.robots.boards.BoardRobotButcher;
import buildcraft.robots.boards.BoardRobotCarrier;
import buildcraft.robots.boards.BoardRobotCrafter;
import buildcraft.robots.boards.BoardRobotDelivery;
import buildcraft.robots.boards.BoardRobotFarmer;
import buildcraft.robots.boards.BoardRobotFluidCarrier;
import buildcraft.robots.boards.BoardRobotHarvester;
import buildcraft.robots.boards.BoardRobotKnight;
import buildcraft.robots.boards.BoardRobotLeaveCutter;
import buildcraft.robots.boards.BoardRobotLumberjack;
import buildcraft.robots.boards.BoardRobotMiner;
import buildcraft.robots.boards.BoardRobotPicker;
import buildcraft.robots.boards.BoardRobotPlanter;
import buildcraft.robots.boards.BoardRobotPump;
import buildcraft.robots.boards.BoardRobotShovelman;
import buildcraft.robots.boards.BoardRobotStripes;

@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", acceptedMinecraftVersions = "[1.7.10,1.8)", dependencies = "required-after:Forge@[10.13.0.1236,)")
public class BuildCraftCore extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Core")
	public static BuildCraftCore instance;

	public static final boolean NONRELEASED_BLOCKS = true;

	public static enum RenderMode {
		Full, NoDynamic
	}
	public static RenderMode render = RenderMode.Full;
	public static boolean debugWorldgen = false;
	public static boolean modifyWorld = false;
	public static boolean colorBlindMode = false;
	public static boolean hidePowerNumbers = false;
	public static boolean hideFluidNumbers = false;
	public static int itemLifespan = 1200;
	public static int updateFactor = 10;
	public static long longUpdateFactor = 40;
	public static BuildCraftConfiguration mainConfiguration;

	public static Block springBlock;
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;
	public static Item wrenchItem;
	public static Item mapLocationItem;
	public static Item listItem;
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

	public static Achievement woodenGearAchievement;
	public static Achievement stoneGearAchievement;
	public static Achievement ironGearAchievement;
	public static Achievement goldGearAchievement;
	public static Achievement diamondGearAchievement;
	public static Achievement wrenchAchievement;
	public static Achievement engineAchievement1;
	public static Achievement engineAchievement2;
	public static Achievement engineAchievement3;
	public static Achievement aLotOfCraftingAchievement;
	public static Achievement straightDownAchievement;
	public static Achievement chunkDestroyerAchievement;
	public static Achievement fasterFillingAchievement;
	public static Achievement timeForSomeLogicAchievement;
	public static Achievement refineAndRedefineAchievement;
	public static Achievement tinglyLaserAchievement;

	public static Achievement architectAchievement;
	public static Achievement builderAchievement;
	public static Achievement blueprintAchievement;
	public static Achievement templateAchievement;
	public static Achievement libraryAchievement;

	public static AchievementPage BuildcraftAchievements;

	public static HashSet<String> recipesBlacklist = new HashSet<String>();

	public static float diffX, diffY, diffZ;

	public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

	private static FloatBuffer modelviewF;
	private static FloatBuffer projectionF;
	private static IntBuffer viewport;

	private static FloatBuffer pos = ByteBuffer.allocateDirect(3 * 4).asFloatBuffer();

	public Thread serverThread;

	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		BCLog.initLog();

		BuildcraftRecipeRegistry.assemblyTable = AssemblyRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.integrationTable = IntegrationRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.refinery = RefineryRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.programmingTable = ProgrammingRecipeManager.INSTANCE;

		BuildcraftFuelRegistry.fuel = FuelManager.INSTANCE;
		BuildcraftFuelRegistry.coolant = CoolantManager.INSTANCE;

		BuilderAPI.schematicRegistry = SchematicRegistry.INSTANCE;
		
		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.conf"));
		try {
			mainConfiguration.load();

			Property updateCheck = BuildCraftCore.mainConfiguration.get("general", "update.check", true);
			updateCheck.comment = "set to true for version check on startup";
			if (updateCheck.getBoolean(true)) {
				Version.check();
			}

			Property hideRFNumbers = BuildCraftCore.mainConfiguration.get("general", "hidePowerNumbers", false);
			hideRFNumbers.comment = "set to true to not display any RF or RF/t numbers.";
			hidePowerNumbers = hideRFNumbers.getBoolean(false);
			
			Property hideMBNumbers = BuildCraftCore.mainConfiguration.get("general", "hideFluidNumbers", false);
			hideMBNumbers.comment = "set to true to not display any mB or mB/t numbers.";
			hideFluidNumbers = hideMBNumbers.getBoolean(false);

			Property lifespan = BuildCraftCore.mainConfiguration.get("general", "itemLifespan", itemLifespan);
			lifespan.comment = "the lifespan in ticks of items dropped on the ground by pipes and machines, vanilla = 6000, default = 1200";
			itemLifespan = lifespan.getInt(itemLifespan);
			if (itemLifespan < 100) {
				itemLifespan = 100;
			}

			Property factor = BuildCraftCore.mainConfiguration.get("general", "network.updateFactor", 10);
			factor.comment = "increasing this number will decrease network update frequency, useful for overloaded servers";
			updateFactor = factor.getInt(10);

			Property longFactor = BuildCraftCore.mainConfiguration.get("general", "network.stateRefreshPeriod", 40);
			longFactor.comment = "delay between full client sync packets, increasing it saves bandwidth, decreasing makes for better client syncronization.";
			longUpdateFactor = longFactor.getInt(40);

			wrenchItem = (new ItemWrench()).setUnlocalizedName("wrenchItem");
			CoreProxy.proxy.registerItem(wrenchItem);

			mapLocationItem = (new ItemMapLocation()).setUnlocalizedName("mapLocation");
			CoreProxy.proxy.registerItem(mapLocationItem);

			listItem = (new ItemList()).setUnlocalizedName("list");
			CoreProxy.proxy.registerItem(listItem);

			Property modifyWorldProp = BuildCraftCore.mainConfiguration.get("general", "modifyWorld", true);
			modifyWorldProp.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";
			modifyWorld = modifyWorldProp.getBoolean(true);

			if (BuildCraftCore.modifyWorld) {
				BlockSpring.EnumSpring.WATER.canGen = BuildCraftCore.mainConfiguration.get("worldgen", "waterSpring", true).getBoolean(true);
				springBlock = new BlockSpring().setBlockName("eternalSpring");
				CoreProxy.proxy.registerBlock(springBlock, ItemSpring.class);
			}

			Property consumeWater = BuildCraftCore.mainConfiguration.get("general", "consumeWater", consumeWaterSources);
			consumeWaterSources = consumeWater.getBoolean(consumeWaterSources);
			consumeWater.comment = "set to true if the Pump should consume water";

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

			MinecraftForge.EVENT_BUS.register(this);
			MinecraftForge.EVENT_BUS.register(new BlockHighlightHandler());
		} finally {
			if (mainConfiguration.hasChanged()) {
				mainConfiguration.save();
			}
		}

	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		BuildCraftAPI.proxy = CoreProxy.proxy;

		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-CORE", new BuildCraftChannelHandler(), new PacketHandler());

		// BuildCraft 6.1.4 and below - migration only
		StatementManager.registerParameterClass("buildcraft:stackTrigger", StatementParameterItemStack.class);
		StatementManager.registerParameterClass("buildcraft:stackAction", StatementParameterItemStack.class);
				
		StatementManager.registerParameterClass(StatementParameterItemStack.class);
		StatementManager.registerParameterClass(StatementParameterDirection.class);
		StatementManager.registerParameterClass(StatementParameterRedstoneGateSideOnly.class);
		StatementManager.registerTriggerProvider(new DefaultTriggerProvider());
		StatementManager.registerActionProvider(new DefaultActionProvider());

		RobotManager.registerAIRobot(AIRobotMain.class, "aiRobotMain", "buildcraft.core.robots.AIRobotMain");
		RobotManager.registerAIRobot(BoardRobotBomber.class, "boardRobotBomber", "buildcraft.core.robots.boards.BoardRobotBomber");
		RobotManager.registerAIRobot(BoardRobotBuilder.class, "boardRobotBuilder", "buildcraft.core.robots.boards.BoardRobotBuilder");
		RobotManager.registerAIRobot(BoardRobotButcher.class, "boardRobotButcher", "buildcraft.core.robots.boards.BoardRobotButcher");
		RobotManager.registerAIRobot(BoardRobotCarrier.class, "boardRobotCarrier", "buildcraft.core.robots.boards.BoardRobotCarrier");
		RobotManager.registerAIRobot(BoardRobotCrafter.class, "boardRobotCrafter", "buildcraft.core.robots.boards.BoardRobotCrafter");
		RobotManager.registerAIRobot(BoardRobotDelivery.class, "boardRobotDelivery", "buildcraft.core.robots.boards.BoardRobotDelivery");
		RobotManager.registerAIRobot(BoardRobotFarmer.class, "boardRobotFarmer", "buildcraft.core.robots.boards.BoardRobotFarmer");
		RobotManager.registerAIRobot(BoardRobotFluidCarrier.class, "boardRobotFluidCarrier", "buildcraft.core.robots.boards.BoardRobotFluidCarrier");
		RobotManager.registerAIRobot(BoardRobotHarvester.class, "boardRobotHarvester", "buildcraft.core.robots.boards.BoardRobotHarvester");
		RobotManager.registerAIRobot(BoardRobotKnight.class, "boardRobotKnight", "buildcraft.core.robots.boards.BoardRobotKnight");
		RobotManager.registerAIRobot(BoardRobotLeaveCutter.class, "boardRobotLeaveCutter", "buildcraft.core.robots.boards.BoardRobotLeaveCutter");
		RobotManager.registerAIRobot(BoardRobotLumberjack.class, "boardRobotLumberjack", "buildcraft.core.robots.boards.BoardRobotLumberjack");
		RobotManager.registerAIRobot(BoardRobotMiner.class, "boardRobotMiner", "buildcraft.core.robots.boards.BoardRobotMiner");
		RobotManager.registerAIRobot(BoardRobotPicker.class, "boardRobotPicker", "buildcraft.core.robots.boards.BoardRobotPicker");
		RobotManager.registerAIRobot(BoardRobotPlanter.class, "boardRobotPlanter", "buildcraft.core.robots.boards.BoardRobotPlanter");
		RobotManager.registerAIRobot(BoardRobotPump.class, "boardRobotPump", "buildcraft.core.robots.boards.BoardRobotPump");
		RobotManager.registerAIRobot(BoardRobotShovelman.class, "boardRobotShovelman", "buildcraft.core.robots.boards.BoardRobotShovelman");
		RobotManager.registerAIRobot(BoardRobotStripes.class, "boardRobotStripes", "buildcraft.core.robots.boards.BoardRobotStripes");
		RobotManager.registerAIRobot(AIRobotAttack.class, "aiRobotAttack", "buildcraft.core.robots.AIRobotAttack");
		RobotManager.registerAIRobot(AIRobotBreak.class, "aiRobotBreak", "buildcraft.core.robots.AIRobotBreak");
		RobotManager.registerAIRobot(AIRobotCraftAssemblyTable.class, "aiRobotCraftAssemblyTable", "buildcraft.core.robots.AIRobotCraftAssemblyTable");
		RobotManager.registerAIRobot(AIRobotCraftFurnace.class, "aiRobotCraftFurnace", "buildcraft.core.robots.AIRobotCraftFurnace");
		RobotManager.registerAIRobot(AIRobotCraftWorkbench.class, "aiRobotCraftWorkbench", "buildcraft.core.robots.AIRobotCraftWorkbench");
		RobotManager.registerAIRobot(AIRobotDeliverRequested.class, "aiRobotDeliverRequested", "buildcraft.core.robots.AIRobotDeliverRequested");
		RobotManager.registerAIRobot(AIRobotDisposeItems.class, "aiRobotDisposeItems", "buildcraft.core.robots.AIRobotDisposeItems");
		RobotManager.registerAIRobot(AIRobotFetchAndEquipItemStack.class, "aiRobotFetchAndEquipItemStack", "buildcraft.core.robots.AIRobotFetchAndEquipItemStack");
		RobotManager.registerAIRobot(AIRobotFetchItem.class, "aiRobotFetchItem", "buildcraft.core.robots.AIRobotFetchItem");
		RobotManager.registerAIRobot(AIRobotGoAndLinkToDock.class, "aiRobotGoAndLinkToDock", "buildcraft.core.robots.AIRobotGoAndLinkToDock");
		RobotManager.registerAIRobot(AIRobotGoto.class, "aiRobotGoto", "buildcraft.core.robots.AIRobotGoto");
		RobotManager.registerAIRobot(AIRobotGotoBlock.class, "aiRobotGotoBlock", "buildcraft.core.robots.AIRobotGotoBlock");
		RobotManager.registerAIRobot(AIRobotGotoSleep.class, "aiRobotGotoSleep", "buildcraft.core.robots.AIRobotGotoSleep");
		RobotManager.registerAIRobot(AIRobotGotoStation.class, "aiRobotGotoStation", "buildcraft.core.robots.AIRobotGotoStation");
		RobotManager.registerAIRobot(AIRobotGotoStationAndLoad.class, "aiRobotGotoStationAndLoad", "buildcraft.core.robots.AIRobotGotoStationAndLoad");
		RobotManager.registerAIRobot(AIRobotGotoStationAndLoadFluids.class, "aiRobotGotoStationAndLoadFluids", "buildcraft.core.robots.AIRobotGotoStationAndLoadFluids");
		RobotManager.registerAIRobot(AIRobotGotoStationAndUnload.class, "aiRobotGotoStationAndUnload", "buildcraft.core.robots.AIRobotGotoStationAndUnload");
		RobotManager.registerAIRobot(AIRobotGotoStationToLoad.class, "aiRobotGotoStationToLoad", "buildcraft.core.robots.AIRobotGotoStationToLoad");
		RobotManager.registerAIRobot(AIRobotGotoStationToLoadFluids.class, "aiRobotGotoStationToLoadFluids", "buildcraft.core.robots.AIRobotGotoStationToLoadFluids");
		RobotManager.registerAIRobot(AIRobotGotoStationToUnload.class, "aiRobotGotoStationToUnload", "buildcraft.core.robots.AIRobotGotoStationToUnload");
		RobotManager.registerAIRobot(AIRobotGotoStationToUnloadFluids.class, "aiRobotGotoStationToUnloadFluids", "buildcraft.core.robots.AIRobotGotoStationToUnloadFluids");
		RobotManager.registerAIRobot(AIRobotLoad.class, "aiRobotLoad", "buildcraft.core.robots.AIRobotLoad");
		RobotManager.registerAIRobot(AIRobotLoadFluids.class, "aiRobotLoadFluids", "buildcraft.core.robots.AIRobotLoadFluids");
		RobotManager.registerAIRobot(AIRobotPumpBlock.class, "aiRobotPumpBlock", "buildcraft.core.robots.AIRobotPumpBlock");
		RobotManager.registerAIRobot(AIRobotRecharge.class, "aiRobotRecharge", "buildcraft.core.robots.AIRobotRecharge");
		RobotManager.registerAIRobot(AIRobotSearchAndGotoStation.class, "aiRobotSearchAndGotoStation", "buildcraft.core.robots.AIRobotSearchAndGotoStation");
		RobotManager.registerAIRobot(AIRobotSearchBlock.class, "aiRobotSearchBlock", "buildcraft.core.robots.AIRobotSearchBlock");
		RobotManager.registerAIRobot(AIRobotSearchEntity.class, "aiRobotSearchEntity", "buildcraft.core.robots.AIRobotSearchEntity");
		RobotManager.registerAIRobot(AIRobotSearchRandomBlock.class, "aiRobotSearchRandomBlock", "buildcraft.core.robots.AIRobotSearchRandomBlock");
		RobotManager.registerAIRobot(AIRobotSearchRandomGroundBlock.class, "aiRobotSearchRandomGroundBlock", "buildcraft.core.robots.AIRobotSearchRandomGroundBlock");
		RobotManager.registerAIRobot(AIRobotSearchStackRequest.class, "aiRobotSearchStackRequest", "buildcraft.core.robots.AIRobotSearchStackRequest");
		RobotManager.registerAIRobot(AIRobotSearchStation.class, "aiRobotSearchStation", "buildcraft.core.robots.AIRobotSearchStation");
		RobotManager.registerAIRobot(AIRobotSleep.class, "aiRobotSleep", "buildcraft.core.robots.AIRobotSleep");
		RobotManager.registerAIRobot(AIRobotStraightMoveTo.class, "aiRobotStraightMoveTo", "buildcraft.core.robots.AIRobotStraightMoveTo");
		RobotManager.registerAIRobot(AIRobotUnload.class, "aiRobotUnload", "buildcraft.core.robots.AIRobotUnload");
		RobotManager.registerAIRobot(AIRobotUnloadFluids.class, "aiRobotUnloadFluids", "buildcraft.core.robots.AIRobotUnloadFluids");
		RobotManager.registerAIRobot(AIRobotUseToolOnBlock.class, "aiRobotUseToolOnBlock", "buildcraft.core.robots.AIRobotUseToolOnBlock");
		RobotManager.registerResourceId(ResourceIdAssemblyTable.class, "resourceIdAssemblyTable", "buildcraft.core.robots.ResourceIdAssemblyTable");
		RobotManager.registerResourceId(ResourceIdBlock.class, "resourceIdBlock", "buildcraft.core.robots.ResourceIdBlock");
		RobotManager.registerResourceId(ResourceIdRequest.class, "resourceIdRequest", "buildcraft.core.robots.ResourceIdRequest");

		if (BuildCraftCore.modifyWorld) {
			MinecraftForge.EVENT_BUS.register(new SpringPopulate());
		}

		for (String l : BuildCraftCore.mainConfiguration.get("general",
				"recipesBlacklist", new String[0]).getStringList()) {
			recipesBlacklist.add(JavaTools.stripSurroundingQuotes(l.trim()));
		}

		if (mainConfiguration.hasChanged()) {
			mainConfiguration.save();
		}

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
		EntityRegistry.registerModEntity(EntityRobot.class, "bcRobot", EntityIds.ROBOT, instance, 50, 1, true);
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

		FMLCommonHandler.instance().bus().register(new CraftingHandler());

		CoreProxy.proxy.initializeRendering();
		CoreProxy.proxy.initializeEntityRendering();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
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

		BuildCraftAPI.isSoftProperty = new WorldPropertyIsSoft();
		BuildCraftAPI.isWoodProperty = new WorldPropertyIsWood();
		BuildCraftAPI.isLeavesProperty = new WorldPropertyIsLeaf();
		BuildCraftAPI.isOreProperty = new IWorldProperty[4];
		for (int i = 0; i < BuildCraftAPI.isOreProperty.length; i++) {
			BuildCraftAPI.isOreProperty[i] = new WorldPropertyIsOre(i);
		}
		BuildCraftAPI.isHarvestableProperty = new WorldPropertyIsHarvestable();
		BuildCraftAPI.isFarmlandProperty = new WorldPropertyIsFarmland();
		BuildCraftAPI.isShoveled = new WorldPropertyIsShoveled();
		BuildCraftAPI.isDirtProperty = new WorldPropertyIsDirt();
		BuildCraftAPI.isFluidSource = new WorldPropertyIsFluidSource();
		
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
		serverThread = Thread.currentThread();
		event.registerServerCommand(new CommandBuildCraft());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() == 1) {
			iconProvider = new CoreIconProvider();
			iconProvider.registerIcons(event.map);
			EnumColor.registerIcons(event.map);
		} else if (event.map.getTextureType() == 0) {
			BuildCraftCore.redLaserTexture = event.map.registerIcon("buildcraft:blockRedLaser");
			BuildCraftCore.blueLaserTexture = event.map.registerIcon("buildcraft:blockBlueLaser");
			BuildCraftCore.stripesLaserTexture = event.map.registerIcon("buildcraft:blockStripesLaser");
			BuildCraftCore.transparentTexture = event.map.registerIcon("buildcraft:blockTransparentLaser");
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
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(listItem), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y',
				"dyeGreen");
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
		// to operate it only when releval (e.g. in the cycle following a
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
		BuildCraftAPI.isSoftProperty.clear();
		BuildCraftAPI.isWoodProperty.clear();
		BuildCraftAPI.isLeavesProperty.clear();
		for (int i = 0; i < BuildCraftAPI.isOreProperty.length; i++) {
			BuildCraftAPI.isOreProperty[i].clear();
		}
		BuildCraftAPI.isHarvestableProperty.clear();
		BuildCraftAPI.isFarmlandProperty.clear();
		BuildCraftAPI.isShoveled.clear();
		BuildCraftAPI.isDirtProperty.clear();
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		woodenGearAchievement = new Achievement("achievement.woodenGear", "woodenGearAchievement", 0, 0, woodenGearItem, null).registerStat();
		stoneGearAchievement = new Achievement("achievement.stoneGear", "stoneGearAchievement", 2, 0, stoneGearItem, woodenGearAchievement).registerStat();
		ironGearAchievement = new Achievement("achievement.ironGear", "ironGearAchievement", 4, 0, ironGearItem, stoneGearAchievement).registerStat();
		goldGearAchievement = new Achievement("achievement.goldGear", "goldGearAchievement", 6, 0, goldGearItem, ironGearAchievement).registerStat();
		diamondGearAchievement = new Achievement("achievement.diamondGear", "diamondGearAchievement", 8, 0, diamondGearItem, goldGearAchievement).registerStat();
		wrenchAchievement = new Achievement("achievement.wrench", "wrenchAchievement", 3, 2, wrenchItem, stoneGearAchievement).registerStat();
		engineAchievement1 = new Achievement("achievement.redstoneEngine", "engineAchievement1", 1, -2, new ItemStack(BuildCraftEnergy.engineBlock, 1, 0), woodenGearAchievement).registerStat();
		engineAchievement2 = new Achievement("achievement.stirlingEngine", "engineAchievement2", 3, -2, new ItemStack(BuildCraftEnergy.engineBlock, 1, 1), engineAchievement1).registerStat();
		engineAchievement3 = new Achievement("achievement.combustionEngine", "engineAchievement3", 5, -2, new ItemStack(BuildCraftEnergy.engineBlock, 1, 2), engineAchievement2).registerStat();
		aLotOfCraftingAchievement = new Achievement("achievement.aLotOfCrafting", "aLotOfCraftingAchievement", 1, 2, BuildCraftFactory.autoWorkbenchBlock, woodenGearAchievement).registerStat();
		straightDownAchievement = new Achievement("achievement.straightDown", "straightDownAchievement", 5, 2, BuildCraftFactory.miningWellBlock, ironGearAchievement).registerStat();
		chunkDestroyerAchievement = new Achievement("achievement.chunkDestroyer", "chunkDestroyerAchievement", 9, 2, BuildCraftFactory.quarryBlock, diamondGearAchievement).registerStat();
		fasterFillingAchievement = new Achievement("achievement.fasterFilling", "fasterFillingAchievement", 7, 2, BuildCraftBuilders.fillerBlock, goldGearAchievement).registerStat();
		timeForSomeLogicAchievement = new Achievement("achievement.timeForSomeLogic", "timeForSomeLogicAchievement", 9, -2, BuildCraftSilicon.assemblyTableBlock, diamondGearAchievement).registerStat();
		refineAndRedefineAchievement = new Achievement("achievement.refineAndRedefine", "refineAndRedefineAchievement", 10, 0, BuildCraftFactory.refineryBlock, diamondGearAchievement).registerStat();
		tinglyLaserAchievement = new Achievement("achievement.tinglyLaser", "tinglyLaserAchievement", 11, -2, BuildCraftSilicon.laserBlock,
				timeForSomeLogicAchievement).registerStat();
		architectAchievement = new Achievement("achievement.architect", "architectAchievement", 11, 2, BuildCraftBuilders.architectBlock, chunkDestroyerAchievement).registerStat();
		builderAchievement = new Achievement("achievement.builder", "builderAchievement", 13, 2, BuildCraftBuilders.builderBlock, architectAchievement).registerStat();
        blueprintAchievement = new Achievement("achievement.blueprint", "blueprintAchievement", 11, 4, BuildCraftBuilders.blueprintItem, architectAchievement).registerStat();
        templateAchievement = new Achievement("achievement.template", "templateAchievement", 13, 4, BuildCraftBuilders.templateItem, blueprintAchievement).registerStat();
        libraryAchievement = new Achievement("achievement.blueprintLibrary", "blueprintLibraryAchievement", 15, 2, BuildCraftBuilders.libraryBlock, builderAchievement).registerStat();

		BuildcraftAchievements = new AchievementPage("Buildcraft", woodenGearAchievement, stoneGearAchievement, ironGearAchievement, goldGearAchievement, diamondGearAchievement, wrenchAchievement, engineAchievement1, engineAchievement2, engineAchievement3, aLotOfCraftingAchievement, straightDownAchievement, chunkDestroyerAchievement, fasterFillingAchievement, timeForSomeLogicAchievement, refineAndRedefineAchievement, tinglyLaserAchievement, architectAchievement, builderAchievement, blueprintAchievement, templateAchievement, libraryAchievement);
		AchievementPage.registerAchievementPage(BuildcraftAchievements);
	}
}
