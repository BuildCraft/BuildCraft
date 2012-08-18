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
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemWrench;
import buildcraft.core.ProxyCore;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.TriggerInventory;
import buildcraft.core.TriggerLiquidContainer;
import buildcraft.core.TriggerMachine;
import buildcraft.core.ActionMachineControl.Mode;
//import buildcraft.core.network.ConnectionHandler;
import buildcraft.core.network.PacketUpdate;
import buildcraft.transport.TriggerRedstoneInput;

import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class BuildCraftCore {

	public static enum RenderMode {
		Full, NoDynamic
	};

	public static RenderMode render = RenderMode.Full;

	public static boolean debugMode = false;
	public static boolean modifyWorld = false;
	public static boolean trackNetworkUsage = false;

	public static boolean dropBrokenBlocks = true; // Set to false to prevent
													// the filler from dropping
													// broken blocks.

	public static int updateFactor = 10;

	public static BuildCraftConfiguration mainConfiguration;

	public static TreeMap<BlockIndex, PacketUpdate> bufferedDescriptions = new TreeMap<BlockIndex, PacketUpdate>();

	public static final int trackedPassiveEntityId = 156;

	public static boolean continuousCurrentModel;

	private static boolean initialized = false;
	private static boolean gearsInitialized = false;

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
	public static Trigger triggerEmptyInventory = new TriggerInventory(DefaultProps.TRIGGER_EMPTY_INVENTORY,
			TriggerInventory.State.Empty);
	public static Trigger triggerContainsInventory = new TriggerInventory(DefaultProps.TRIGGER_CONTAINS_INVENTORY,
			TriggerInventory.State.Contains);
	public static Trigger triggerSpaceInventory = new TriggerInventory(DefaultProps.TRIGGER_SPACE_INVENTORY,
			TriggerInventory.State.Space);
	public static Trigger triggerFullInventory = new TriggerInventory(DefaultProps.TRIGGER_FULL_INVENTORY,
			TriggerInventory.State.Full);
	public static Trigger triggerEmptyLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_EMPTY_LIQUID,
			TriggerLiquidContainer.State.Empty);
	public static Trigger triggerContainsLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_CONTAINS_LIQUID,
			TriggerLiquidContainer.State.Contains);
	public static Trigger triggerSpaceLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_SPACE_LIQUID,
			TriggerLiquidContainer.State.Space);
	public static Trigger triggerFullLiquid = new TriggerLiquidContainer(DefaultProps.TRIGGER_FULL_LIQUID,
			TriggerLiquidContainer.State.Full);
	public static Trigger triggerRedstoneActive = new TriggerRedstoneInput(DefaultProps.TRIGGER_REDSTONE_ACTIVE, true);
	public static Trigger triggerRedstoneInactive = new TriggerRedstoneInput(DefaultProps.TRIGGER_REDSTONE_INACTIVE, false);

	public static Action actionRedstone = new ActionRedstoneOutput(DefaultProps.ACTION_REDSTONE);
	public static Action actionOn = new ActionMachineControl(DefaultProps.ACTION_ON, Mode.On);
	public static Action actionOff = new ActionMachineControl(DefaultProps.ACTION_OFF, Mode.Off);
	public static Action actionLoop = new ActionMachineControl(DefaultProps.ACTION_LOOP, Mode.Loop);

	public static boolean loadDefaultRecipes = true;
	public static boolean forcePneumaticPower = false;
	public static boolean consumeWaterSources = true;

	public static BptItem[] itemBptProps = new BptItem[Item.itemsList.length];

	public static void load() {

		//MinecraftForge.registerConnectionHandler(new ConnectionHandler());
	}

	public static void initialize() {
		if (initialized)
			return;

		ModLoader.getLogger().fine("Starting BuildCraft " + DefaultProps.VERSION);
		ModLoader.getLogger().fine("Copyright (c) SpaceToad, 2011");
		ModLoader.getLogger().fine("http://www.mod-buildcraft.com");

		System.out.println("Starting BuildCraft " + DefaultProps.VERSION);
		System.out.println("Copyright (c) SpaceToad, 2011-2012");
		System.out.println("http://www.mod-buildcraft.com");

		initialized = true;

		mainConfiguration = new BuildCraftConfiguration(new File(ProxyCore.proxy.getBuildCraftBase(), "config/buildcraft.cfg"), true);
		mainConfiguration.load();

		redLaserTexture = 0 * 16 + 2;
		blueLaserTexture = 0 * 16 + 1;
		stripesLaserTexture = 0 * 16 + 3;
		transparentTexture = 0 * 16 + 0;

		Property continuousCurrent = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("current.continuous",
				Configuration.CATEGORY_GENERAL, DefaultProps.CURRENT_CONTINUOUS);
		continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";

		continuousCurrentModel = Boolean.parseBoolean(continuousCurrent.value);

		Property trackNetwork = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("trackNetworkUsage",
				Configuration.CATEGORY_GENERAL, false);

		trackNetworkUsage = Boolean.parseBoolean(trackNetwork.value);

		Property dropBlock = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("dropBrokenBlocks",
				Configuration.CATEGORY_GENERAL, true);
		dropBlock.comment = "set to false to prevent fillers from dropping blocks.";
		dropBrokenBlocks = Boolean.parseBoolean(dropBlock.value);

		Property powerFrameworkClass = BuildCraftCore.mainConfiguration.getOrCreateProperty("power.framework",
				Configuration.CATEGORY_GENERAL, "buildcraft.energy.PneumaticPowerFramework");

		Property factor = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("network.updateFactor",
				Configuration.CATEGORY_GENERAL, 10);
		factor.comment = "increasing this number will decrease network update frequency, useful for overloaded servers";

		updateFactor = Integer.parseInt(factor.value);

		String prefix = "";

		if (BuildCraftCore.class.getName().startsWith("net.minecraft.src."))
			prefix = "net.minecraft.src.";

		if (forcePneumaticPower)
			try {
				PowerFramework.currentFramework = (PowerFramework) Class
						.forName(prefix + "buildcraft.energy.PneumaticPowerFramework").getConstructor().newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		else
			try {
				String className = powerFrameworkClass.value;
				if (className.startsWith("net.minecraft.src."))
					className = className.replace("net.minecraft.src.", "");

				PowerFramework.currentFramework = (PowerFramework) Class.forName(prefix + className).getConstructor()
						.newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
				PowerFramework.currentFramework = new RedstonePowerFramework();
			}

		Property wrenchId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("wrench.id", Configuration.CATEGORY_ITEM,
				DefaultProps.WRENCH_ID);

		mainConfiguration.save();

		initializeGears();

		wrenchItem = (new ItemWrench(Integer.parseInt(wrenchId.value))).setIconIndex(0 * 16 + 2).setItemName("wrenchItem");
		ProxyCore.proxy.addName(wrenchItem, "Wrench");

		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.waterStill, BuildCraftAPI.BUCKET_VOLUME), new LiquidStack(Block.waterMoving, BuildCraftAPI.BUCKET_VOLUME), new ItemStack(Item.bucketWater), new ItemStack(Item.bucketEmpty)));
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(Block.lavaStill, BuildCraftAPI.BUCKET_VOLUME), new LiquidStack(Block.lavaMoving, BuildCraftAPI.BUCKET_VOLUME), new ItemStack(Item.bucketLava), new ItemStack(Item.bucketEmpty)));

		BuildCraftAPI.softBlocks[Block.tallGrass.blockID] = true;
		BuildCraftAPI.softBlocks[Block.snow.blockID] = true;
		BuildCraftAPI.softBlocks[Block.waterMoving.blockID] = true;
		BuildCraftAPI.softBlocks[Block.waterStill.blockID] = true;

		mainConfiguration.save();

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();
	}

	public static void loadRecipes() {

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(wrenchItem), new Object[] { "I I", " G ", " I ", Character.valueOf('I'),
				Item.ingotIron, Character.valueOf('G'), stoneGearItem });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(woodenGearItem), new Object[] { " S ", "S S", " S ", Character.valueOf('S'),
				Item.stick });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(stoneGearItem), new Object[] { " I ", "IGI", " I ", Character.valueOf('I'),
				Block.cobblestone, Character.valueOf('G'), woodenGearItem });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(ironGearItem), new Object[] { " I ", "IGI", " I ", Character.valueOf('I'),
				Item.ingotIron, Character.valueOf('G'), stoneGearItem });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(goldGearItem), new Object[] { " I ", "IGI", " I ", Character.valueOf('I'),
				Item.ingotGold, Character.valueOf('G'), ironGearItem });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(diamondGearItem), new Object[] { " I ", "IGI", " I ", Character.valueOf('I'),
				Item.diamond, Character.valueOf('G'), goldGearItem });
	}

	public static void initializeGears() {
		if (gearsInitialized)
			return;

		Property woodenGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("woodenGearItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.WOODEN_GEAR_ID);
		Property stoneGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("stoneGearItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.STONE_GEAR_ID);
		Property ironGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("ironGearItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.IRON_GEAR_ID);
		Property goldenGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("goldenGearItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.GOLDEN_GEAR_ID);
		Property diamondGearId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("diamondGearItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.DIAMOND_GEAR_ID);
		Property modifyWorld = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("modifyWorld",
				Configuration.CATEGORY_GENERAL, true);
		modifyWorld.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";

		BuildCraftCore.mainConfiguration.save();

		BuildCraftCore.modifyWorld = modifyWorld.value.equals("true");

		gearsInitialized = true;

		woodenGearItem = (new ItemBuildCraft(Integer.parseInt(woodenGearId.value))).setIconIndex(1 * 16 + 0).setItemName(
				"woodenGearItem");
		ProxyCore.proxy.addName(woodenGearItem, "Wooden Gear");

		stoneGearItem = (new ItemBuildCraft(Integer.parseInt(stoneGearId.value))).setIconIndex(1 * 16 + 1).setItemName(
				"stoneGearItem");
		ProxyCore.proxy.addName(stoneGearItem, "Stone Gear");

		ironGearItem = (new ItemBuildCraft(Integer.parseInt(ironGearId.value))).setIconIndex(1 * 16 + 2).setItemName(
				"ironGearItem");
		ProxyCore.proxy.addName(ironGearItem, "Iron Gear");

		goldGearItem = (new ItemBuildCraft(Integer.parseInt(goldenGearId.value))).setIconIndex(1 * 16 + 3).setItemName(
				"goldGearItem");
		ProxyCore.proxy.addName(goldGearItem, "Gold Gear");

		diamondGearItem = (new ItemBuildCraft(Integer.parseInt(diamondGearId.value))).setIconIndex(1 * 16 + 4).setItemName(
				"diamondGearItem");
		ProxyCore.proxy.addName(diamondGearItem, "Diamond Gear");

		BuildCraftCore.mainConfiguration.save();

		ActionManager.registerTriggerProvider(new DefaultTriggerProvider());
		ActionManager.registerActionProvider(new DefaultActionProvider());
	}

	public static void initializeModel(BaseMod mod) {
		blockByEntityModel = ModLoader.getUniqueBlockModelID(mod, true);
		legacyPipeModel = ModLoader.getUniqueBlockModelID(mod, true);
		markerModel = ModLoader.getUniqueBlockModelID(mod, false);
		oilModel = ModLoader.getUniqueBlockModelID(mod, false);
	}

}
