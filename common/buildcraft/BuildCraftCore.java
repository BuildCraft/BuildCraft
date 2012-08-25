/**
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

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.api.liquids.LiquidData;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.ActionMachineControl;
import buildcraft.core.ActionRedstoneOutput;
import buildcraft.core.BlockIndex;
import buildcraft.core.BptItem;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.DefaultActionProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.DefaultTriggerProvider;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemWrench;
import buildcraft.core.ProxyCore;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.TriggerInventory;
import buildcraft.core.TriggerLiquidContainer;
import buildcraft.core.TriggerMachine;
import buildcraft.core.ActionMachineControl.Mode;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.PacketHandler;
//import buildcraft.core.network.ConnectionHandler;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.utils.Localization;
import buildcraft.transport.TriggerRedstoneInput;

import net.minecraft.src.Block;
import net.minecraft.src.EntityList;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

@Mod(name="BuildCraft", version=DefaultProps.VERSION, useMetadata = false, modid = "BuildCraft|Core")
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

	@Instance
	public static BuildCraftCore instance;

	@PreInit
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		bcLog.setParent(FMLLog.getLogger());
		bcLog.info("Starting BuildCraft " + DefaultProps.VERSION);
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

			Property continuousCurrent = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("current.continuous", Configuration.CATEGORY_GENERAL, DefaultProps.CURRENT_CONTINUOUS);
			continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";
			continuousCurrentModel = continuousCurrent.getBoolean(DefaultProps.CURRENT_CONTINUOUS);

			Property trackNetwork = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("trackNetworkUsage", Configuration.CATEGORY_GENERAL, false);
			trackNetworkUsage = trackNetwork.getBoolean(false);

			Property dropBlock = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("dropBrokenBlocks", Configuration.CATEGORY_GENERAL, true);
			dropBlock.comment = "set to false to prevent fillers from dropping blocks.";
			dropBrokenBlocks = dropBlock.getBoolean(true);

			Property powerFrameworkClass = BuildCraftCore.mainConfiguration.getOrCreateProperty("power.framework", Configuration.CATEGORY_GENERAL, "buildcraft.energy.PneumaticPowerFramework");

			Property factor = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("network.updateFactor", Configuration.CATEGORY_GENERAL, 10);
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

			Property wrenchId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("wrench.id", Configuration.CATEGORY_ITEM, DefaultProps.WRENCH_ID);

			wrenchItem = (new ItemWrench(wrenchId.getInt(DefaultProps.WRENCH_ID))).setIconIndex(0 * 16 + 2).setItemName("wrenchItem");
			LanguageRegistry.addName(wrenchItem, "Wrench");

			Property woodenGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("woodenGearItem.id", Configuration.CATEGORY_ITEM, DefaultProps.WOODEN_GEAR_ID);
			Property stoneGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("stoneGearItem.id", Configuration.CATEGORY_ITEM, DefaultProps.STONE_GEAR_ID);
			Property ironGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("ironGearItem.id", Configuration.CATEGORY_ITEM, DefaultProps.IRON_GEAR_ID);
			Property goldenGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("goldenGearItem.id", Configuration.CATEGORY_ITEM, DefaultProps.GOLDEN_GEAR_ID);
			Property diamondGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("diamondGearItem.id", Configuration.CATEGORY_ITEM, DefaultProps.DIAMOND_GEAR_ID);
			Property modifyWorld = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("modifyWorld", Configuration.CATEGORY_GENERAL, true);
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
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.waterStill, BuildCraftAPI.BUCKET_VOLUME), new LiquidStack(Block.waterMoving, BuildCraftAPI.BUCKET_VOLUME), new ItemStack(Item.bucketWater), new ItemStack(Item.bucketEmpty)));
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.lavaStill, BuildCraftAPI.BUCKET_VOLUME), new LiquidStack(Block.lavaMoving, BuildCraftAPI.BUCKET_VOLUME), new ItemStack(Item.bucketLava), new ItemStack(Item.bucketEmpty)));

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
		EntityRegistry.registerModEntity(EntityLaser.class, "bcLaser", EntityIds.LASER, instance, 50, 1, true);
		EntityRegistry.registerModEntity(EntityEnergyLaser.class, "bcEnergyLaser", EntityIds.ENERGY_LASER, instance, 50, 1, true);
		EntityList.classToStringMapping.remove(EntityRobot.class);
		EntityList.classToStringMapping.remove(EntityLaser.class);
		EntityList.classToStringMapping.remove(EntityEnergyLaser.class);
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcRobot");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

		ProxyCore.proxy.initializeRendering();
		ProxyCore.proxy.initializeEntityRendering();

		Localization.addLocalization("/lang/buildcraft/", DefaultProps.DEFAULT_LANGUAGE);

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
