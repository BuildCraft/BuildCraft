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

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fluids.IFluidBlock;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.CommandBuildCraft;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityPowerLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemSpring;
import buildcraft.core.ItemWrench;
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
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.triggers.DefaultActionProvider;
import buildcraft.core.triggers.DefaultTriggerProvider;
import buildcraft.core.triggers.TriggerFluidContainer;
import buildcraft.core.triggers.TriggerInventory;
import buildcraft.core.triggers.TriggerInventoryLevel;
import buildcraft.core.triggers.TriggerMachine;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.Localization;
import buildcraft.transport.triggers.TriggerRedstoneInput;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", acceptedMinecraftVersions = "[1.6.4,1.7)", dependencies = "required-after:Forge@[9.11.1.953,)")
@NetworkMod(channels = { DefaultProps.NET_CHANNEL_NAME }, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftCore {
	public static enum RenderMode {
		Full, NoDynamic
	};

	public static RenderMode render = RenderMode.Full;

	public static boolean debugMode = false;
	public static boolean modifyWorld = false;
	public static boolean trackNetworkUsage = false;
	public static boolean colorBlindMode = false;

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

	@SideOnly(Side.CLIENT)
	public static Icon redLaserTexture;
	@SideOnly(Side.CLIENT)
	public static Icon blueLaserTexture;
	@SideOnly(Side.CLIENT)
	public static Icon stripesLaserTexture;
	@SideOnly(Side.CLIENT)
	public static Icon transparentTexture;

	@SideOnly(Side.CLIENT)
	public static IIconProvider iconProvider;

	public static int blockByEntityModel;
	public static int legacyPipeModel;
	public static int markerModel;
	public static int oilModel;

	public static BCTrigger triggerMachineActive = new TriggerMachine(DefaultProps.TRIGGER_MACHINE_ACTIVE, true);
	public static BCTrigger triggerMachineInactive = new TriggerMachine(DefaultProps.TRIGGER_MACHINE_INACTIVE, false);
	public static BCTrigger triggerEmptyInventory = new TriggerInventory(DefaultProps.TRIGGER_EMPTY_INVENTORY, TriggerInventory.State.Empty);
	public static BCTrigger triggerContainsInventory = new TriggerInventory(DefaultProps.TRIGGER_CONTAINS_INVENTORY, TriggerInventory.State.Contains);
	public static BCTrigger triggerSpaceInventory = new TriggerInventory(DefaultProps.TRIGGER_SPACE_INVENTORY, TriggerInventory.State.Space);
	public static BCTrigger triggerFullInventory = new TriggerInventory(DefaultProps.TRIGGER_FULL_INVENTORY, TriggerInventory.State.Full);
	public static BCTrigger triggerEmptyFluid = new TriggerFluidContainer(DefaultProps.TRIGGER_EMPTY_LIQUID, TriggerFluidContainer.State.Empty);
	public static BCTrigger triggerContainsFluid = new TriggerFluidContainer(DefaultProps.TRIGGER_CONTAINS_LIQUID, TriggerFluidContainer.State.Contains);
	public static BCTrigger triggerSpaceFluid = new TriggerFluidContainer(DefaultProps.TRIGGER_SPACE_LIQUID, TriggerFluidContainer.State.Space);
	public static BCTrigger triggerFullFluid = new TriggerFluidContainer(DefaultProps.TRIGGER_FULL_LIQUID, TriggerFluidContainer.State.Full);
	public static BCTrigger triggerRedstoneActive = new TriggerRedstoneInput(DefaultProps.TRIGGER_REDSTONE_ACTIVE, true);
	public static BCTrigger triggerRedstoneInactive = new TriggerRedstoneInput(DefaultProps.TRIGGER_REDSTONE_INACTIVE, false);

	public static BCTrigger triggerInventoryBelow25 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_25);
	public static BCTrigger triggerInventoryBelow50 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_50);
	public static BCTrigger triggerInventoryBelow75 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_75);

	public static BCAction actionRedstone = new ActionRedstoneOutput(DefaultProps.ACTION_REDSTONE);
	public static BCAction actionOn = new ActionMachineControl(DefaultProps.ACTION_ON, Mode.On);
	public static BCAction actionOff = new ActionMachineControl(DefaultProps.ACTION_OFF, Mode.Off);
	public static BCAction actionLoop = new ActionMachineControl(DefaultProps.ACTION_LOOP, Mode.Loop);

	public static boolean loadDefaultRecipes = true;
	public static boolean forcePneumaticPower = true;
	public static boolean consumeWaterSources = false;

	public static BptItem[] itemBptProps = new BptItem[Item.itemsList.length];

	@Instance("BuildCraft|Core")
	public static BuildCraftCore instance;

	@EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {

		BCLog.initLog();

		BlueprintDatabase.init(evt.getModConfigurationDirectory());

		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.conf"));
		try {
			mainConfiguration.load();

			Property updateCheck = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "update.check", true);
			updateCheck.comment = "set to true for version check on startup";
			if (updateCheck.getBoolean(true)) {
				Version.check();
			}

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

			Property wrenchId = BuildCraftCore.mainConfiguration.getItem("wrench.id", DefaultProps.WRENCH_ID);

			wrenchItem = (new ItemWrench(wrenchId.getInt(DefaultProps.WRENCH_ID))).setUnlocalizedName("wrenchItem");
			LanguageRegistry.addName(wrenchItem, "Wrench");
			CoreProxy.proxy.registerItem(wrenchItem);

			Property springId = BuildCraftCore.mainConfiguration.getBlock("springBlock.id", DefaultProps.SPRING_ID);

			Property woodenGearId = BuildCraftCore.mainConfiguration.getItem("woodenGearItem.id", DefaultProps.WOODEN_GEAR_ID);
			Property stoneGearId = BuildCraftCore.mainConfiguration.getItem("stoneGearItem.id", DefaultProps.STONE_GEAR_ID);
			Property ironGearId = BuildCraftCore.mainConfiguration.getItem("ironGearItem.id", DefaultProps.IRON_GEAR_ID);
			Property goldenGearId = BuildCraftCore.mainConfiguration.getItem("goldenGearItem.id", DefaultProps.GOLDEN_GEAR_ID);
			Property diamondGearId = BuildCraftCore.mainConfiguration.getItem("diamondGearItem.id", DefaultProps.DIAMOND_GEAR_ID);
			Property modifyWorldProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "modifyWorld", true);
			modifyWorldProp.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";
			modifyWorld = modifyWorldProp.getBoolean(true);

			Property consumeWater = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "consumeWater", consumeWaterSources);
			consumeWaterSources = consumeWater.getBoolean(consumeWaterSources);
			consumeWater.comment = "set to true if the Pump should consume water";

			if(BuildCraftCore.modifyWorld) {
				springBlock = new BlockSpring(springId.getInt()).setUnlocalizedName("eternalSpring");
				CoreProxy.proxy.registerBlock(springBlock, ItemSpring.class);
			}

			woodenGearItem = (new ItemBuildCraft(woodenGearId.getInt())).setUnlocalizedName("woodenGearItem");
			LanguageRegistry.addName(woodenGearItem, "Wooden Gear");
			CoreProxy.proxy.registerItem(woodenGearItem);
			OreDictionary.registerOre("gearWood", new ItemStack(woodenGearItem));

			stoneGearItem = (new ItemBuildCraft(stoneGearId.getInt())).setUnlocalizedName("stoneGearItem");
			LanguageRegistry.addName(stoneGearItem, "Stone Gear");
			CoreProxy.proxy.registerItem(stoneGearItem);
			OreDictionary.registerOre("gearStone", new ItemStack(stoneGearItem));

			ironGearItem = (new ItemBuildCraft(ironGearId.getInt())).setUnlocalizedName("ironGearItem");
			LanguageRegistry.addName(ironGearItem, "Iron Gear");
			CoreProxy.proxy.registerItem(ironGearItem);
			OreDictionary.registerOre("gearIron", new ItemStack(ironGearItem));

			goldGearItem = (new ItemBuildCraft(goldenGearId.getInt())).setUnlocalizedName("goldGearItem");
			LanguageRegistry.addName(goldGearItem, "Gold Gear");
			CoreProxy.proxy.registerItem(goldGearItem);
			OreDictionary.registerOre("gearGold", new ItemStack(goldGearItem));

			diamondGearItem = (new ItemBuildCraft(diamondGearId.getInt())).setUnlocalizedName("diamondGearItem");
			LanguageRegistry.addName(diamondGearItem, "Diamond Gear");
			CoreProxy.proxy.registerItem(diamondGearItem);
			OreDictionary.registerOre("gearDiamond", new ItemStack(diamondGearItem));

			Property colorBlindProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "client.colorblindmode", false);
			colorBlindProp.comment = "Set to true to enable alternate textures";
			colorBlindMode = colorBlindProp.getBoolean(false);

			MinecraftForge.EVENT_BUS.register(this);

		} finally {
			if (mainConfiguration.hasChanged()) {
				mainConfiguration.save();
			}
		}
	}

	@EventHandler
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

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (Block block : Block.blocksList) {
			if (block instanceof BlockFluid || block instanceof IFluidBlock || block instanceof IPlantable) {
				BuildCraftAPI.softBlocks[block.blockID] = true;
			}
		}

		BuildCraftAPI.softBlocks[Block.snow.blockID] = true;
		BuildCraftAPI.softBlocks[Block.vine.blockID] = true;
		BuildCraftAPI.softBlocks[Block.fire.blockID] = true;
		TickRegistry.registerTickHandler(new TickHandlerCoreClient(), Side.CLIENT);

	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuildCraft());
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event){
		if (event.map.textureType == 1) {
			iconProvider = new CoreIconProvider();
			iconProvider.registerIcons(event.map);
			ActionTriggerIconProvider.INSTANCE.registerIcons(event.map);
		} else if (event.map.textureType == 0) {
			BuildCraftCore.redLaserTexture = event.map.registerIcon("buildcraft:blockRedLaser");
			BuildCraftCore.blueLaserTexture = event.map.registerIcon("buildcraft:blockBlueLaser");
			BuildCraftCore.stripesLaserTexture = event.map.registerIcon("buildcraft:blockStripesLaser");
			BuildCraftCore.transparentTexture = event.map.registerIcon("buildcraft:blockTransparentLaser");
		}

	}

	public void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", 'I', Item.ingotIron, 'G', stoneGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(woodenGearItem), " S ", "S S", " S ", 'S', "stickWood");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(stoneGearItem), " I ", "IGI", " I ", 'I', "cobblestone", 'G',
				woodenGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(ironGearItem), " I ", "IGI", " I ", 'I', Item.ingotIron, 'G', stoneGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(goldGearItem), " I ", "IGI", " I ", 'I', Item.ingotGold, 'G', ironGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(diamondGearItem), " I ", "IGI", " I ", 'I', Item.diamond, 'G', goldGearItem);
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}
}
