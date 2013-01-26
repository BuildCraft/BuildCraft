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

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Property;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.CommandBuildCraft;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityPowerLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemWrench;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.SpringPopulate;
import buildcraft.core.TickHandlerCoreClient;
import buildcraft.core.Version;
import buildcraft.core.blueprints.BptItem;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.triggers.ActionMachineControl.Mode;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.core.triggers.DefaultActionProvider;
import buildcraft.core.triggers.DefaultTriggerProvider;
import buildcraft.core.triggers.TriggerInventory;
import buildcraft.core.triggers.TriggerLiquidContainer;
import buildcraft.core.triggers.TriggerMachine;
import buildcraft.core.utils.Localization;
import buildcraft.transport.triggers.TriggerRedstoneInput;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", dependencies = "required-after:Forge@[6.3.0.0,)")
@NetworkMod(channels = { DefaultProps.NET_CHANNEL_NAME }, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftCore {
	public static enum RenderMode {
		Full, NoDynamic
	};

	public static RenderMode render = RenderMode.Full;

	public static boolean debugMode = false;
	public static boolean modifyWorld = false;
	public static boolean trackNetworkUsage = false;

	public static boolean dropBrokenBlocks = true; // Set to false to prevent the filler from dropping broken blocks.

	public static int itemLifespan = 1200;

	public static int updateFactor = 10;

	public static long longUpdateFactor = 40;

	public static BuildCraftConfiguration mainConfiguration;

	public static TreeMap<BlockIndex, PacketUpdate> bufferedDescriptions = new TreeMap<BlockIndex, PacketUpdate>();

	public static final int trackedPassiveEntityId = 156;

	public static boolean continuousCurrentModel;

	public static Block springBlock;
	
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

		Version.check();

		bcLog.setParent(FMLLog.getLogger());
		bcLog.info("Starting BuildCraft " + Version.getVersion());
		bcLog.info("Copyright (c) SpaceToad, 2011");
		bcLog.info("http://www.mod-buildcraft.com");

		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.conf"));
		try {
			mainConfiguration.load();

			redLaserTexture = 0 * 16 + 2;
			blueLaserTexture = 0 * 16 + 1;
			stripesLaserTexture = 0 * 16 + 3;
			transparentTexture = 0 * 16 + 0;

			Property continuousCurrent = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "current.continuous",
					DefaultProps.CURRENT_CONTINUOUS);
			continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";
			continuousCurrentModel = continuousCurrent.getBoolean(DefaultProps.CURRENT_CONTINUOUS);

			Property trackNetwork = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "trackNetworkUsage", false);
			trackNetworkUsage = trackNetwork.getBoolean(false);

			Property dropBlock = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "dropBrokenBlocks", true);
			dropBlock.comment = "set to false to prevent fillers from dropping blocks.";
			dropBrokenBlocks = dropBlock.getBoolean(true);

			Property lifespan = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "itemLifespan", itemLifespan);
			lifespan.comment = "the lifespan in ticks of items dropped on the ground by pipes and machines, vanilla = 6000, default = 1200";
			itemLifespan = lifespan.getInt(itemLifespan);
			if (itemLifespan < 100) {
				itemLifespan = 100;
			}

			Property powerFrameworkClass = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "power.framework",
					"buildcraft.energy.PneumaticPowerFramework");

			Property factor = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "network.updateFactor", 10);
			factor.comment = "increasing this number will decrease network update frequency, useful for overloaded servers";
			updateFactor = factor.getInt(10);

			Property longFactor = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "network.stateRefreshPeriod", 40);
			longFactor.comment = "delay between full client sync packets, increasing it saves bandwidth, decreasing makes for better client syncronization.";
			longUpdateFactor = longFactor.getInt(40);

			String powerFrameworkClassName = "buildcraft.energy.PneumaticPowerFramework";
			if (!forcePneumaticPower) {
				powerFrameworkClassName = powerFrameworkClass.value;
			}
			try {
				PowerFramework.currentFramework = (PowerFramework) Class.forName(powerFrameworkClassName).getConstructor().newInstance();
			} catch (Throwable e) {
				bcLog.throwing("BuildCraftCore", "loadConfiguration", e);
				PowerFramework.currentFramework = new RedstonePowerFramework();
			}

			Property wrenchId = BuildCraftCore.mainConfiguration.getItem("wrench.id", DefaultProps.WRENCH_ID);

			wrenchItem = (new ItemWrench(wrenchId.getInt(DefaultProps.WRENCH_ID))).setIconIndex(0 * 16 + 2).setItemName("wrenchItem");
			LanguageRegistry.addName(wrenchItem, "Wrench");

			Property springId = BuildCraftCore.mainConfiguration.getBlock("springBlock.id", DefaultProps.SPRING_ID);
			
			Property woodenGearId = BuildCraftCore.mainConfiguration.getItem("woodenGearItem.id", DefaultProps.WOODEN_GEAR_ID);
			Property stoneGearId = BuildCraftCore.mainConfiguration.getItem("stoneGearItem.id", DefaultProps.STONE_GEAR_ID);
			Property ironGearId = BuildCraftCore.mainConfiguration.getItem("ironGearItem.id", DefaultProps.IRON_GEAR_ID);
			Property goldenGearId = BuildCraftCore.mainConfiguration.getItem("goldenGearItem.id", DefaultProps.GOLDEN_GEAR_ID);
			Property diamondGearId = BuildCraftCore.mainConfiguration.getItem("diamondGearItem.id", DefaultProps.DIAMOND_GEAR_ID);
			Property modifyWorld = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "modifyWorld", true);
			modifyWorld.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";

			BuildCraftCore.modifyWorld = modifyWorld.getBoolean(true);

			if(BuildCraftCore.modifyWorld) {
				springBlock = new BlockSpring(Integer.parseInt(springId.value)).setBlockName("eternalSpring");
				GameRegistry.registerBlock(springBlock, "eternalSpring");
			}
			
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
		} finally {
			mainConfiguration.save();
		}
	}

	@Init
	public void initialize(FMLInitializationEvent evt) {
		// MinecraftForge.registerConnectionHandler(new ConnectionHandler());
		ActionManager.registerTriggerProvider(new DefaultTriggerProvider());
		ActionManager.registerActionProvider(new DefaultActionProvider());

		if (BuildCraftCore.modifyWorld) {
			MinecraftForge.EVENT_BUS.register(new SpringPopulate());
		}

		if (BuildCraftCore.loadDefaultRecipes) {
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

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		for (Block block : Block.blocksList) {
			if (block instanceof BlockFluid || block instanceof IPlantable) {
				BuildCraftAPI.softBlocks[block.blockID] = true;
			}
		}

		BuildCraftAPI.softBlocks[Block.snow.blockID] = true;
		BuildCraftAPI.softBlocks[Block.vine.blockID] = true;
		TickRegistry.registerTickHandler(new TickHandlerCoreClient(), Side.CLIENT);

	}

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuildCraft());
	}

	public void loadRecipes() {
		GameRegistry.addRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), stoneGearItem);
		GameRegistry.addRecipe(new ItemStack(woodenGearItem), " S ", "S S", " S ", Character.valueOf('S'), Item.stick);
		GameRegistry.addRecipe(new ItemStack(stoneGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone, Character.valueOf('G'),
				woodenGearItem);
		GameRegistry.addRecipe(new ItemStack(ironGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), stoneGearItem);
		GameRegistry.addRecipe(new ItemStack(goldGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold, Character.valueOf('G'), ironGearItem);
		GameRegistry.addRecipe(new ItemStack(diamondGearItem), " I ", "IGI", " I ", Character.valueOf('I'), Item.diamond, Character.valueOf('G'), goldGearItem);
	}
}
