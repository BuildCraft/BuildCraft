/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 *
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */



package buildcraft;

import java.io.File;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.LinkedList;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.api.liquids.LiquidData;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.BlockIndex;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.CommandBuildCraft;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityPowerLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemWrench;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.blueprints.BptItem;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.core.triggers.DefaultActionProvider;
import buildcraft.core.triggers.DefaultTriggerProvider;
import buildcraft.core.triggers.TriggerInventory;
import buildcraft.core.triggers.TriggerLiquidContainer;
import buildcraft.core.triggers.TriggerMachine;
import buildcraft.core.triggers.ActionMachineControl.Mode;
import buildcraft.core.utils.Localization;
import buildcraft.transport.triggers.TriggerRedstoneInput;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.bptblocks.BptBlockBed;
import buildcraft.api.bptblocks.BptBlockCustomStack;
import buildcraft.api.bptblocks.BptBlockDelegate;
import buildcraft.api.bptblocks.BptBlockDirt;
import buildcraft.api.bptblocks.BptBlockDoor;
import buildcraft.api.bptblocks.BptBlockIgnore;
import buildcraft.api.bptblocks.BptBlockIgnoreMeta;
import buildcraft.api.bptblocks.BptBlockInventory;
import buildcraft.api.bptblocks.BptBlockLever;
import buildcraft.api.bptblocks.BptBlockLiquid;
import buildcraft.api.bptblocks.BptBlockPiston;
import buildcraft.api.bptblocks.BptBlockPumpkin;
import buildcraft.api.bptblocks.BptBlockRedstoneRepeater;
import buildcraft.api.bptblocks.BptBlockRotateInventory;
import buildcraft.api.bptblocks.BptBlockRotateMeta;
import buildcraft.api.bptblocks.BptBlockSign;
import buildcraft.api.bptblocks.BptBlockStairs;
import buildcraft.api.bptblocks.BptBlockWallSide;
import buildcraft.api.filler.FillerManager;
import buildcraft.builders.BlockArchitect;
import buildcraft.builders.BlockBlueprintLibrary;
import buildcraft.builders.BlockBuilder;
import buildcraft.builders.BlockFiller;
import buildcraft.builders.BlockMarker;
import buildcraft.builders.BlockPathMarker;
import buildcraft.builders.BptBlockFiller;
import buildcraft.builders.EventHandlerBuilders;
import buildcraft.builders.FillerFillAll;
import buildcraft.builders.FillerFillPyramid;
import buildcraft.builders.FillerFillStairs;
import buildcraft.builders.FillerFillWalls;
import buildcraft.builders.FillerFlattener;
import buildcraft.builders.FillerRegistry;
import buildcraft.builders.FillerRemover;
import buildcraft.builders.GuiHandler;
import buildcraft.builders.IBuilderHook;
import buildcraft.builders.ItemBptBluePrint;
import buildcraft.builders.ItemBptTemplate;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileFiller;
import buildcraft.builders.TileMarker;
import buildcraft.builders.TilePathMarker;
import buildcraft.builders.network.PacketHandlerBuilders;
import buildcraft.core.DefaultProps;
import buildcraft.core.Version;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.blueprints.BptRootIndex;

import net.minecraft.src.Block;
import net.minecraft.src.CommandHandler;
import net.minecraft.src.EntityList;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraft.src.Block;
import net.minecraft.src.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;



@Mod(name="BuildCraft", version=Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", dependencies="required-after:Forge@[5.0,)")
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftCore {
	public static enum RenderMode {
		Full, NoDynamic
	};

	public static RenderMode render = RenderMode.Full;

	public static boolean debugMode = false;
	public static boolean modifyWorld = false;
	public static boolean trackNetworkUsage = false;

	public static boolean dropBrokenBlocks = true; // Set to false to prevent the filler from dropping broken blocks.

	public static int updateFactor = 10;

	public static BuildCraftConfiguration mainConfiguration;

	public static TreeMap<BlockIndex, PacketUpdate> bufferedDescriptions = new TreeMap<BlockIndex, PacketUpdate>();

	public static final int trackedPassiveEntityId = 156;

	public static boolean continuousCurrentModel;

	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;
	public static Item wrenchItem;

	public static int redLaserTexture;
	public static int blueLaserTexture;
	public static int stripesLaserTexture;
	public static int transparentTexture;

	public static int blockByEntityModel;
	public static int legacyPipeModel;
	public static int markerModel;
	public static int oilModel;

	public static Trigger triggerMachineActive = new TriggerMachine(DefaultProps.TRIGGER_MACHINE_ACTIVE, true);
	public static Trigger triggerMachineInactive = new TriggerMachine(DefaultProps.TRIGGER_MACHINE_INACTIVE, false);
	public static Trigger triggerEmptyInventory = new TriggerInventory(DefaultProps.TRIGGER_EMPTY_INVENTORY, TriggerInventory.State.Empty);
	public static Trigger triggerContainsInventory = new TriggerInventory(DefaultProps.TRIGGER_CONTAINS_INVENTORY, TriggerInventory.State.Contains);
	public static Trigger triggerSpaceInventory = new TriggerInventory(DefaultProps.TRIGGER_SPACE_INVENTORY, TriggerInventory.State.Space);
	public static Trigger triggerFullInventory = new TriggerInventory(DefaultProps.TRIGGER_FULL_INVENTORY, TriggerInventory.State.Full);
	public static Trigger triggerEmptyLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_EMPTY_LIQUID, TriggerLiquidContainer.State.Empty);
	public static Trigger triggerContainsLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_CONTAINS_LIQUID, TriggerLiquidContainer.State.Contains);
	public static Trigger triggerSpaceLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_SPACE_LIQUID, TriggerLiquidContainer.State.Space);
	public static Trigger triggerFullLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_FULL_LIQUID, TriggerLiquidContainer.State.Full);
	public static Trigger triggerRedstoneActive = new TriggerRedstoneInput(DefaultProps.TRIGGER_REDSTONE_ACTIVE, true);
	public static Trigger triggerRedstoneInactive = new TriggerRedstoneInput(DefaultProps.TRIGGER_REDSTONE_INACTIVE, false);

	public static Action actionRedstone = new ActionRedstoneOutput(DefaultProps.ACTION_REDSTONE);
	public static Action actionOn = new ActionMachineControl(DefaultProps.ACTION_ON, Mode.On);
	public static Action actionOff = new ActionMachineControl(DefaultProps.ACTION_OFF, Mode.Off);
	public static Action actionLoop = new ActionMachineControl(DefaultProps.ACTION_LOOP, Mode.Loop);

	public static boolean loadDefaultRecipes = true;
	public static boolean forcePneumaticPower = true;
	public static boolean consumeWaterSources = true;

	public static BptItem[] itemBptProps = new BptItem[Item.itemsList.length];

	public static Logger bcLog = Logger.getLogger("Buildcraft");

	@Instance("BuildCraft|Core")
	public static BuildCraftCore instance;

	@PreInit
	public void loadConfiguration(FMLPreInitializationEvent evt) {

		Version.versionCheck();

		bcLog.setParent(FMLLog.getLogger());
		bcLog.info("Starting BuildCraft " + Version.getVersion());
		bcLog.info("Copyright (c) SpaceToad, 2011");
		bcLog.info("http://www.mod-buildcraft.com");

		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.conf"));
		try
		{
			mainConfiguration.load();

			redLaserTexture = 0 * 16 + 2;
			blueLaserTexture = 0 * 16 + 1;
			stripesLaserTexture = 0 * 16 + 3;
			transparentTexture = 0 * 16 + 0;

			Property continuousCurrent = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"current.continuous", DefaultProps.CURRENT_CONTINUOUS);
			continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";
			continuousCurrentModel = continuousCurrent.getBoolean(DefaultProps.CURRENT_CONTINUOUS);

			Property trackNetwork = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"trackNetworkUsage", false);
			trackNetworkUsage = trackNetwork.getBoolean(false);

			Property dropBlock = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"dropBrokenBlocks", true);
			dropBlock.comment = "set to false to prevent fillers from dropping blocks.";
			dropBrokenBlocks = dropBlock.getBoolean(true);

			Property powerFrameworkClass = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"power.framework", "buildcraft.energy.PneumaticPowerFramework");

			Property factor = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"network.updateFactor", 10);
			factor.comment = "increasing this number will decrease network update frequency, useful for overloaded servers";
			updateFactor = factor.getInt(10);

			String powerFrameworkClassName = "buildcraft.energy.PneumaticPowerFramework";
			if (!forcePneumaticPower)
			{
				powerFrameworkClassName = powerFrameworkClass.value;
			}
			try {
				PowerFramework.currentFramework = (PowerFramework) Class.forName(powerFrameworkClassName).getConstructor().newInstance();
			} catch (Throwable e) {
				bcLog.throwing("BuildCraftCore", "loadConfiguration", e);
				PowerFramework.currentFramework = new RedstonePowerFramework();
			}

			Property wrenchId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"wrench.id", DefaultProps.WRENCH_ID);

			wrenchItem = (new ItemWrench(wrenchId.getInt(DefaultProps.WRENCH_ID))).setIconIndex(0 * 16 + 2).setItemName("wrenchItem");
			LanguageRegistry.addName(wrenchItem, "Wrench");

			Property woodenGearId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"woodenGearItem.id", DefaultProps.WOODEN_GEAR_ID);
			Property stoneGearId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"stoneGearItem.id", DefaultProps.STONE_GEAR_ID);
			Property ironGearId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"ironGearItem.id", DefaultProps.IRON_GEAR_ID);
			Property goldenGearId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"goldenGearItem.id", DefaultProps.GOLDEN_GEAR_ID);
			Property diamondGearId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"diamondGearItem.id", DefaultProps.DIAMOND_GEAR_ID);
			Property modifyWorld = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"modifyWorld", true);
			modifyWorld.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";

			BuildCraftCore.modifyWorld = modifyWorld.getBoolean(true);

			woodenGearItem = (new ItemBuildCraft(Integer.parseInt(woodenGearId.value))).setIconIndex(1 * 16 + 0).setItemName("woodenGearItem");
			LanguageRegistry.addName(woodenGearItem, "Wooden Gear");

			stoneGearItem = (new ItemBuildCraft(Integer.parseInt(stoneGearId.value))).setIconIndex(1 * 16 + 1).setItemName("stoneGearItem");
			LanguageRegistry.addName(stoneGearItem, "Stone Gear");

			ironGearItem = (new ItemBuildCraft(Integer.parseInt(ironGearId.value))).setIconIndex(1 * 16 + 2).setItemName("ironGearItem");
			LanguageRegistry.addName(ironGearItem, "Iron Gear");

			goldGearItem = (new ItemBuildCraft(Integer.parseInt(goldenGearId.value))).setIconIndex(1 * 16 + 3).setItemName("goldGearItem");
			LanguageRegistry.addName(goldGearItem, "Gold Gear");

			diamondGearItem = (new ItemBuildCraft(Integer.parseInt(diamondGearId.value))).setIconIndex(1 * 16 + 4).setItemName("diamondGearItem");
			LanguageRegistry.addName(diamondGearItem, "Diamond Gear");
		}
		finally
		{
			mainConfiguration.save();
		}
	}

	@Init
	public void initialize(FMLInitializationEvent evt) {
		//MinecraftForge.registerConnectionHandler(new ConnectionHandler());
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.waterStill, LiquidManager.BUCKET_VOLUME), new LiquidStack(Block.waterMoving, LiquidManager.BUCKET_VOLUME), new ItemStack(Item.bucketWater), new ItemStack(Item.bucketEmpty)));
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.waterStill, LiquidManager.BUCKET_VOLUME), new LiquidStack(Block.waterMoving, LiquidManager.BUCKET_VOLUME), new ItemStack(Item.potion), new ItemStack(Item.glassBottle)));
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.lavaStill, LiquidManager.BUCKET_VOLUME), new LiquidStack(Block.lavaMoving, LiquidManager.BUCKET_VOLUME), new ItemStack(Item.bucketLava), new ItemStack(Item.bucketEmpty)));

		BuildCraftAPI.softBlocks[Block.tallGrass.blockID] = true;
		BuildCraftAPI.softBlocks[Block.snow.blockID] = true;
		BuildCraftAPI.softBlocks[Block.waterMoving.blockID] = true;
		BuildCraftAPI.softBlocks[Block.waterStill.blockID] = true;

		ActionManager.registerTriggerProvider(new DefaultTriggerProvider());
		ActionManager.registerActionProvider(new DefaultActionProvider());

		if (BuildCraftCore.loadDefaultRecipes)
		{
			loadRecipes();
		}
		EntityRegistry.registerModEntity(EntityRobot.class, "bcRobot", EntityIds.ROBOT, instance, 50, 1, true);
		EntityRegistry.registerModEntity(EntityPowerLaser.class, "bcLaser", EntityIds.LASER, instance, 50, 1, true);
		EntityRegistry.registerModEntity(EntityEnergyLaser.class, "bcEnergyLaser", EntityIds.ENERGY_LASER, instance, 50, 1, true);
		EntityList.classToStringMapping.remove(EntityRobot.class);
		EntityList.classToStringMapping.remove(EntityPowerLaser.class);
		EntityList.classToStringMapping.remove(EntityEnergyLaser.class);
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcRobot");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

		CoreProxy.proxy.initializeRendering();
		CoreProxy.proxy.initializeEntityRendering();

		Localization.addLocalization("/lang/buildcraft/", DefaultProps.DEFAULT_LANGUAGE);

	}

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event) {
		CommandHandler commandManager = (CommandHandler)event.getServer().getCommandManager();
		commandManager.registerCommand(new CommandBuildCraft());
	}

	public void loadRecipes() {
		GameRegistry.addRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), stoneGearItem);
		GameRegistry.addRecipe(new ItemStack(woodenGearItem), " S ", "S S", " S ", Character.valueOf('S'), Item.stick);
		GameRegistry.addRecipe(new ItemStack(stoneGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone, Character.valueOf('G'), woodenGearItem);
		GameRegistry.addRecipe(new ItemStack(ironGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), stoneGearItem);
		GameRegistry.addRecipe(new ItemStack(goldGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold, Character.valueOf('G'), ironGearItem);
		GameRegistry.addRecipe(new ItemStack(diamondGearItem), " I ", "IGI", " I ", Character.valueOf('I'),Item.diamond, Character.valueOf('G'), goldGearItem);
	}
}

//Code to be Merged Below This



@Mod(name="BuildCraft Builders", version=Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerBuilders.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftBuilders {

	public static final int LIBRARY_PAGE_SIZE = 12;

	public static final int MAX_BLUEPRINTS_NAME_SIZE = 14;

	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static ItemBptTemplate templateItem;
	public static ItemBptBluePrint blueprintItem;
	public static boolean fillerDestroy;

	private static BptRootIndex rootBptIndex;

	public static TreeMap<String, BptPlayerIndex> playerLibrary = new TreeMap<String, BptPlayerIndex>();

	private static LinkedList<IBuilderHook> hooks = new LinkedList<IBuilderHook>();

	@Instance("BuildCraft|Builders")
	public static BuildCraftBuilders instance;

	@Init
	public void load(FMLInitializationEvent evt) {
		// Create filler registry
		FillerManager.registry = new FillerRegistry();

		// Register gui handler
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		// Register save handler
		MinecraftForge.EVENT_BUS.register(new EventHandlerBuilders());

		new BptBlock(0); // default bpt block

		new BptBlockIgnore(Block.snow.blockID);
		new BptBlockIgnore(Block.tallGrass.blockID);
		new BptBlockIgnore(Block.ice.blockID);
		new BptBlockIgnore(Block.pistonExtension.blockID);

		new BptBlockDirt(Block.dirt.blockID);
		new BptBlockDirt(Block.grass.blockID);
		new BptBlockDirt(Block.tilledField.blockID);

		new BptBlockDelegate(Block.torchRedstoneIdle.blockID, Block.torchRedstoneActive.blockID);
		new BptBlockDelegate(Block.stoneOvenActive.blockID, Block.stoneOvenIdle.blockID);
		new BptBlockDelegate(Block.pistonMoving.blockID, Block.pistonBase.blockID);

		new BptBlockWallSide(Block.torchWood.blockID);
		new BptBlockWallSide(Block.torchRedstoneActive.blockID);

		new BptBlockRotateMeta(Block.ladder.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateMeta(Block.fenceGate.blockID, new int[] { 0, 1, 2, 3 }, true);

		new BptBlockRotateInventory(Block.stoneOvenIdle.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(Block.chest.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(Block.lockedChest.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(Block.dispenser.blockID, new int[] { 2, 5, 3, 4 }, true);

		new BptBlockInventory(Block.brewingStand.blockID);

		new BptBlockRotateMeta(Block.vine.blockID, new int[] { 1, 4, 8, 2 }, false);
		new BptBlockRotateMeta(Block.trapdoor.blockID, new int[] { 0, 1, 2, 3 }, false);

		new BptBlockLever(Block.button.blockID);
		new BptBlockLever(Block.lever.blockID);

		new BptBlockCustomStack(Block.stone.blockID, new ItemStack(Block.stone));
		new BptBlockCustomStack(Block.redstoneWire.blockID, new ItemStack(Item.redstone));
		// FIXME: Not sure what this has become
		//new BptBlockCustomStack(Block.stairDouble.blockID, new ItemStack(Block.stairSingle, 2));
		new BptBlockCustomStack(Block.cake.blockID, new ItemStack(Item.cake));
		new BptBlockCustomStack(Block.crops.blockID, new ItemStack(Item.seeds));
		new BptBlockCustomStack(Block.pumpkinStem.blockID, new ItemStack(Item.pumpkinSeeds));
		new BptBlockCustomStack(Block.melonStem.blockID, new ItemStack(Item.melonSeeds));
		new BptBlockCustomStack(Block.glowStone.blockID, new ItemStack(Block.glowStone));

		new BptBlockRedstoneRepeater(Block.redstoneRepeaterActive.blockID);
		new BptBlockRedstoneRepeater(Block.redstoneRepeaterIdle.blockID);

		new BptBlockLiquid(Block.waterStill.blockID, new ItemStack(Item.bucketWater));
		new BptBlockLiquid(Block.waterMoving.blockID, new ItemStack(Item.bucketWater));
		new BptBlockLiquid(Block.lavaStill.blockID, new ItemStack(Item.bucketLava));
		new BptBlockLiquid(Block.lavaMoving.blockID, new ItemStack(Item.bucketLava));

		new BptBlockIgnoreMeta(Block.rail.blockID);
		new BptBlockIgnoreMeta(Block.railPowered.blockID);
		new BptBlockIgnoreMeta(Block.railDetector.blockID);
		new BptBlockIgnoreMeta(Block.thinGlass.blockID);

		new BptBlockPiston(Block.pistonBase.blockID);
		new BptBlockPiston(Block.pistonStickyBase.blockID);

		new BptBlockPumpkin(Block.pumpkinLantern.blockID);

		new BptBlockStairs(Block.stairCompactCobblestone.blockID);
		new BptBlockStairs(Block.stairCompactPlanks.blockID);
		new BptBlockStairs(Block.stairsNetherBrick.blockID);
		new BptBlockStairs(Block.stairsBrick.blockID);
		new BptBlockStairs(Block.stairsStoneBrickSmooth.blockID);

		new BptBlockDoor(Block.doorWood.blockID, new ItemStack(Item.doorWood));
		new BptBlockDoor(Block.doorSteel.blockID, new ItemStack(Item.doorSteel));

		new BptBlockBed(Block.bed.blockID);

		new BptBlockSign(Block.signWall.blockID, true);
		new BptBlockSign(Block.signPost.blockID, false);

		// BUILDCRAFT BLOCKS

		new BptBlockRotateInventory(architectBlock.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(builderBlock.blockID, new int[] { 2, 5, 3, 4 }, true);

		new BptBlockInventory(libraryBlock.blockID);

		new BptBlockWallSide(markerBlock.blockID);
		new BptBlockWallSide(pathMarkerBlock.blockID);
		new BptBlockFiller(fillerBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
		{
			loadRecipes();
		}

	}

	@PreInit
	public void initialize(FMLPreInitializationEvent evt) {
		Property templateItemId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"templateItem.id", DefaultProps.TEMPLATE_ITEM_ID);
		Property blueprintItemId = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_ITEM,"blueprintItem.id", DefaultProps.BLUEPRINT_ITEM_ID);
		Property markerId = BuildCraftCore.mainConfiguration.getBlock("marker.id", DefaultProps.MARKER_ID);
		Property pathMarkerId = BuildCraftCore.mainConfiguration.getBlock("pathMarker.id", DefaultProps.PATH_MARKER_ID);
		Property fillerId = BuildCraftCore.mainConfiguration.getBlock("filler.id", DefaultProps.FILLER_ID);
		Property builderId = BuildCraftCore.mainConfiguration.getBlock("builder.id", DefaultProps.BUILDER_ID);
		Property architectId = BuildCraftCore.mainConfiguration.getBlock("architect.id", DefaultProps.ARCHITECT_ID);
		Property libraryId = BuildCraftCore.mainConfiguration.getBlock("blueprintLibrary.id", DefaultProps.BLUEPRINT_LIBRARY_ID);

		Property fillerDestroyProp = BuildCraftCore.mainConfiguration.get( Configuration.CATEGORY_GENERAL,"filler.destroy", DefaultProps.FILLER_DESTROY);
		fillerDestroyProp.comment = "If true, Filler will destroy blocks instead of breaking them.";
		fillerDestroy = fillerDestroyProp.getBoolean(DefaultProps.FILLER_DESTROY);

		templateItem = new ItemBptTemplate(Integer.parseInt(templateItemId.value));
		templateItem.setItemName("templateItem");
		LanguageRegistry.addName(templateItem, "Template");

		blueprintItem = new ItemBptBluePrint(Integer.parseInt(blueprintItemId.value));
		blueprintItem.setItemName("blueprintItem");
		LanguageRegistry.addName(blueprintItem, "Blueprint");

		markerBlock = new BlockMarker(Integer.parseInt(markerId.value));
		CoreProxy.proxy.registerBlock(markerBlock.setBlockName("markerBlock"));
		CoreProxy.proxy.addName(markerBlock, "Land Mark");

		pathMarkerBlock = new BlockPathMarker(Integer.parseInt(pathMarkerId.value));
		CoreProxy.proxy.registerBlock(pathMarkerBlock.setBlockName("pathMarkerBlock"));
		CoreProxy.proxy.addName(pathMarkerBlock, "Path Mark");

		fillerBlock = new BlockFiller(Integer.parseInt(fillerId.value));
		CoreProxy.proxy.registerBlock(fillerBlock.setBlockName("fillerBlock"));
		CoreProxy.proxy.addName(fillerBlock, "Filler");

		builderBlock = new BlockBuilder(Integer.parseInt(builderId.value));
		CoreProxy.proxy.registerBlock(builderBlock.setBlockName("builderBlock"));
		CoreProxy.proxy.addName(builderBlock, "Builder");

		architectBlock = new BlockArchitect(Integer.parseInt(architectId.value));
		CoreProxy.proxy.registerBlock(architectBlock.setBlockName("architectBlock"));
		CoreProxy.proxy.addName(architectBlock, "Architect Table");

		libraryBlock = new BlockBlueprintLibrary(Integer.parseInt(libraryId.value));
		CoreProxy.proxy.registerBlock(libraryBlock.setBlockName("libraryBlock"));
		CoreProxy.proxy.addName(libraryBlock, "Blueprint Library");

		GameRegistry.registerTileEntity(TileMarker.class, "Marker");
		GameRegistry.registerTileEntity(TileFiller.class, "Filler");
		GameRegistry.registerTileEntity(TileBuilder.class, "net.minecraft.src.builders.TileBuilder");
		GameRegistry.registerTileEntity(TileArchitect.class, "net.minecraft.src.builders.TileTemplate");
		GameRegistry.registerTileEntity(TilePathMarker.class, "net.minecraft.src.builders.TilePathMarker");
		GameRegistry.registerTileEntity(TileBlueprintLibrary.class, "net.minecraft.src.builders.TileBlueprintLibrary");

		BuildCraftCore.mainConfiguration.save();

		// public static final Block music;
		// public static final Block cloth;
		// public static final Block tilledField;
		// public static final BlockPortal portal;
		// public static final Block trapdoor;

		// STANDARD BLOCKS
	}

	public static void loadRecipes() {

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), new Object[] { "ppp", "pip", "ppp", Character.valueOf('i'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('p'), Item.paper });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(blueprintItem, 1), new Object[] { "ppp", "pip", "ppp", Character.valueOf('i'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('p'), Item.paper });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), new Object[] { "l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('r'), Block.torchRedstoneActive });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), new Object[] { "l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 2), Character.valueOf('r'), Block.torchRedstoneActive });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'),
				new ItemStack(Item.dyePowder, 1, 11), Character.valueOf('c'), Block.workbench, Character.valueOf('g'),
				BuildCraftCore.goldGearItem, Character.valueOf('C'), Block.chest });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'),
				new ItemStack(Item.dyePowder, 1, 11), Character.valueOf('c'), Block.workbench, Character.valueOf('g'),
				BuildCraftCore.diamondGearItem, Character.valueOf('C'), Block.chest });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'),
				new ItemStack(Item.dyePowder, 1, 11), Character.valueOf('c'), Block.workbench, Character.valueOf('g'),
				BuildCraftCore.diamondGearItem, Character.valueOf('C'), new ItemStack(templateItem, 1) });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(libraryBlock, 1), new Object[] { "bbb", "bBb", "bbb", Character.valueOf('b'),
				new ItemStack(blueprintItem), Character.valueOf('B'), Block.bookShelf });
		// / INIT FILLER PATTERNS
		FillerManager.registry.addRecipe(new FillerFillAll(), new Object[] { "bbb", "bbb", "bbb", Character.valueOf('b'),
				new ItemStack(Block.brick, 1) });

		FillerManager.registry.addRecipe(new FillerFlattener(), new Object[] { "   ", "ggg", "bbb", Character.valueOf('g'),
				Block.glass, Character.valueOf('b'), Block.brick });

		FillerManager.registry.addRecipe(new FillerRemover(), new Object[] { "ggg", "ggg", "ggg", Character.valueOf('g'),
				Block.glass });

		FillerManager.registry.addRecipe(new FillerFillWalls(), new Object[] { "bbb", "b b", "bbb", Character.valueOf('b'),
				Block.brick });

		FillerManager.registry.addRecipe(new FillerFillPyramid(), new Object[] { "   ", " b ", "bbb", Character.valueOf('b'),
				Block.brick });

		FillerManager.registry.addRecipe(new FillerFillStairs(), new Object[] { "  b", " bb", "bbb", Character.valueOf('b'),
				Block.brick });
	}

	public static BptPlayerIndex getPlayerIndex(String name) {
		BptRootIndex rootIndex = getBptRootIndex();

		if (!playerLibrary.containsKey(name))
			try {
				playerLibrary.put(name, new BptPlayerIndex(name + ".list", rootIndex));
			} catch (IOException e) {
				e.printStackTrace();
			}

		return playerLibrary.get(name);
	}

	public static BptRootIndex getBptRootIndex() {
		if (rootBptIndex == null)
			try {
				rootBptIndex = new BptRootIndex("index.txt");
				rootBptIndex.loadIndex();

				for (IBuilderHook hook : hooks)
					hook.rootIndexInitialized(rootBptIndex);

				rootBptIndex.importNewFiles();

			} catch (IOException e) {
				e.printStackTrace();
			}

		return rootBptIndex;
	}

	public static ItemStack getBptItemStack(int id, int damage, String name) {
		ItemStack stack = new ItemStack(id, 1, damage);
		NBTTagCompound nbt = new NBTTagCompound();
		if(name != null && !"".equals(name)) {
			nbt.setString("BptName", name);
			stack.setTagCompound(nbt);
		}
		return stack;
	}

	public static void addHook(IBuilderHook hook) {
		if (!hooks.contains(hook))
			hooks.add(hook);
	}

	@Mod.ServerStopping
	public void ServerStop(FMLServerStoppingEvent event) {
		TilePathMarker.clearAvailableMarkersList();
	}

}
